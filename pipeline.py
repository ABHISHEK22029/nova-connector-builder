"""
pipeline.py
End-to-end orchestrator: parse prompt → retrieve → build context → generate → validate → save.
"""

import os
import sys
import logging
from pathlib import Path
from typing import List, Dict, Optional

from dotenv import load_dotenv

load_dotenv()

from agent.query_processor import parse_prompt, ConnectorSpec
from agent.retriever import RetrievalEngine
from agent.context_builder import build_context
from agent.prompt_templates import (
    SYSTEM_PROMPT,
    FILE_GENERATION_ORDER,
    get_file_task,
    build_generation_prompt,
)
from agent.generator import ConnectorGenerator
from agent.validator import validate_file, validate_all
from agent.feedback import FeedbackLoop
from embeddings.embedder import EmbeddingService
from vectordb.store import VectorStore

logger = logging.getLogger(__name__)

OUTPUT_DIR = Path(os.getenv("OUTPUT_DIR", "./generated"))


def run_pipeline(
    prompt: str,
    output_dir: str = None,
    file_types: List[str] = None,
    skip_validation: bool = False,
    embedder: EmbeddingService = None,
    store: VectorStore = None,
) -> Dict:
    """
    Full connector generation pipeline.

    Args:
        prompt: User prompt (e.g., "Create a Stripe connector with OAuth2")
        output_dir: Where to save generated files
        file_types: Specific files to generate (default: all)
        skip_validation: Skip output validation
        embedder: Shared embedding service
        store: Shared vector store

    Returns:
        Dict with generated files, validation results, and stats.
    """
    # ── 1. Initialize services ──
    if embedder is None:
        embedder = EmbeddingService()
    if store is None:
        store = VectorStore()

    retriever = RetrievalEngine(embedder, store)
    generator = ConnectorGenerator()
    feedback = FeedbackLoop(embedder, store)

    # ── 2. Parse the prompt ──
    logger.info(f"\n{'='*60}")
    logger.info(f"PROMPT: {prompt}")
    logger.info(f"{'='*60}")

    spec = parse_prompt(prompt)
    connector_name = spec.connector_name
    target_files = file_types or FILE_GENERATION_ORDER

    # ── 3. Set up output directory ──
    out_dir = Path(output_dir) if output_dir else OUTPUT_DIR / connector_name.lower()
    out_dir.mkdir(parents=True, exist_ok=True)

    # ── 4. Generate files sequentially ──
    generated_files = []
    total_files = len(target_files)

    for idx, file_type in enumerate(target_files, 1):
        logger.info(f"\n{'─'*50}")
        logger.info(f"[{idx}/{total_files}] Generating: {file_type}")
        logger.info(f"{'─'*50}")

        # Get file task definition
        file_task = get_file_task(file_type, connector_name)

        # Retrieve relevant context
        logger.info("  Retrieving context...")
        chunks = retriever.retrieve_for_file_task(file_type, spec, top_k=5)
        context = build_context(chunks, file_type, connector_name)

        # Build prompt
        prompt_text = build_generation_prompt(
            file_task=file_task,
            context=context,
            previously_generated=generated_files,
        )

        # Generate
        logger.info(f"  Generating {file_task['file_name']}...")
        try:
            content = generator.generate(
                system_prompt=SYSTEM_PROMPT,
                user_prompt=prompt_text,
                max_tokens=8192,
                temperature=0.1,
            )
        except Exception as e:
            logger.error(f"  ❌ Generation failed: {e}")
            content = f"// GENERATION FAILED: {e}"

        # Save
        file_path = out_dir / file_task["file_name"]
        file_path.write_text(content)

        generated_file = {
            "file_name": file_task["file_name"],
            "file_type": file_type,
            "content": content,
            "path": str(file_path),
            "retrieval_count": len(chunks),
        }
        generated_files.append(generated_file)

        logger.info(f"  ✅ Saved: {file_path}")

        # Validate
        if not skip_validation:
            result = validate_file(content, file_task["file_name"], file_type)
            logger.info(f"  {result}")

    # ── 5. Final validation ──
    all_passed, validation_results = validate_all(generated_files)

    logger.info(f"\n{'='*60}")
    logger.info(f"GENERATION COMPLETE: {connector_name}")
    logger.info(f"{'='*60}")
    logger.info(f"Files generated: {len(generated_files)}")
    logger.info(f"Output directory: {out_dir}")
    logger.info(f"Validation: {'✅ ALL PASSED' if all_passed else '❌ SOME FAILED'}")

    for vr in validation_results:
        logger.info(f"  {vr}")

    # ── 6. Store in feedback loop ──
    feedback.store_generation(
        connector_name=connector_name,
        generated_files=generated_files,
        prompt=prompt,
        quality_score=1.0 if all_passed else 0.5,
    )

    return {
        "connector_name": connector_name,
        "files": generated_files,
        "validation_passed": all_passed,
        "validation_results": [str(vr) for vr in validation_results],
        "output_dir": str(out_dir),
        "spec": {
            "auth_type": spec.auth_type,
            "entity_types": spec.entity_types,
            "sync_direction": spec.sync_direction,
        },
    }


# ---------------------------------------------------------------------------
# CLI entry point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(message)s",
    )

    import argparse

    parser = argparse.ArgumentParser(description="Generate a Nova connector")
    parser.add_argument("--connector", required=True, help="Connector name (e.g., stripe)")
    parser.add_argument("--output", default=None, help="Output directory")
    parser.add_argument("--files", nargs="*", default=None, help="Specific files to generate")
    parser.add_argument("--skip-validation", action="store_true")
    args = parser.parse_args()

    prompt = f"Create a {args.connector} connector"
    result = run_pipeline(
        prompt=prompt,
        output_dir=args.output,
        file_types=args.files,
        skip_validation=args.skip_validation,
    )

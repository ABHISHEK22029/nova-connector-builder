"""
agent/feedback.py
Feedback loop — stores generated connectors and user corrections.
Re-ingests corrected files to improve future generation quality.
"""

import os
import json
import logging
from datetime import datetime
from pathlib import Path
from typing import List, Dict, Optional

from embeddings.embedder import EmbeddingService
from vectordb.store import VectorStore, GENERATED_CONNECTORS

logger = logging.getLogger(__name__)

FEEDBACK_DIR = Path(__file__).resolve().parent.parent / "generated" / ".feedback"


class FeedbackLoop:
    """Manages generated connector storage and quality improvement."""

    def __init__(self, embedder: EmbeddingService, store: VectorStore):
        self.embedder = embedder
        self.store = store
        FEEDBACK_DIR.mkdir(parents=True, exist_ok=True)

    def store_generation(
        self,
        connector_name: str,
        generated_files: List[Dict],
        prompt: str,
        quality_score: Optional[float] = None,
    ):
        """
        Store a complete generation run for future reference.
        """
        timestamp = datetime.now().isoformat()
        run_id = f"{connector_name}_{timestamp}"

        # Save to disk
        run_dir = FEEDBACK_DIR / run_id
        run_dir.mkdir(parents=True, exist_ok=True)

        metadata = {
            "connector_name": connector_name,
            "prompt": prompt,
            "timestamp": timestamp,
            "quality_score": quality_score,
            "files": [f["file_name"] for f in generated_files],
        }

        (run_dir / "metadata.json").write_text(json.dumps(metadata, indent=2))

        for gf in generated_files:
            (run_dir / gf["file_name"]).write_text(gf["content"])

        # Index into ChromaDB for retrieval
        texts = [gf["content"] for gf in generated_files]
        metadatas = [
            {
                "connector": connector_name,
                "component_type": gf.get("file_type", "generated"),
                "file_name": gf["file_name"],
                "language": self._detect_language(gf["file_name"]),
                "auth_type": "",
                "entity_type": "",
                "is_generated": "true",
                "quality_score": str(quality_score or 0),
            }
            for gf in generated_files
        ]
        ids = [f"gen_{connector_name}_{gf['file_name']}" for gf in generated_files]

        embeddings = self.embedder.embed_texts(texts)

        self.store.add_documents(
            collection_name=GENERATED_CONNECTORS,
            documents=texts,
            embeddings=embeddings,
            metadatas=metadatas,
            ids=ids,
        )

        logger.info(f"Stored generation run: {run_id} ({len(generated_files)} files)")

    def store_correction(
        self,
        connector_name: str,
        file_name: str,
        corrected_content: str,
    ):
        """
        Store a user correction to re-ingest as improved knowledge.
        """
        from ingest.chunker import chunk_file

        chunks = chunk_file(
            content=corrected_content,
            file_name=file_name,
            connector=connector_name,
        )

        for chunk in chunks:
            chunk["metadata"]["is_corrected"] = "true"

        texts = [c["text"] for c in chunks]
        metadatas = [c["metadata"] for c in chunks]
        ids = [f"corrected_{c['id']}" for c in chunks]

        embeddings = self.embedder.embed_texts(texts)

        self.store.add_documents(
            collection_name=GENERATED_CONNECTORS,
            documents=texts,
            embeddings=embeddings,
            metadatas=metadatas,
            ids=ids,
        )

        logger.info(f"Stored correction: {connector_name}/{file_name}")

    def _detect_language(self, file_name: str) -> str:
        ext = Path(file_name).suffix.lower()
        return {".java": "java", ".js": "javascript", ".xml": "xml", ".sql": "sql"}.get(ext, "text")

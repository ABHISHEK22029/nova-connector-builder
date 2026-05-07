"""
ingest/ingest_framework.py
Ingests framework base classes, golden references, and framework rules into ChromaDB.
This is the MOST IMPORTANT ingestion — without it, the LLM doesn't know platform rules.
"""

import os
import sys
import logging
from pathlib import Path
from typing import List, Dict, Any

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from dotenv import load_dotenv
load_dotenv()

from ingest.chunker import chunk_file, chunk_markdown_file
from embeddings.embedder import EmbeddingService
from vectordb.store import VectorStore, NOVA_FRAMEWORK, CONNECTOR_KNOWLEDGE

logger = logging.getLogger(__name__)

SIH_BASE = Path(os.getenv("SIH_CODEBASE_PATH", "../")).resolve()
BUILDER_BASE = Path(__file__).resolve().parent.parent


# ── Golden Reference Files ──

GOLDEN_DIR = BUILDER_BASE / "knowledge" / "golden"

def collect_golden_references() -> List[Dict[str, Any]]:
    """Collect annotated golden reference connector files."""
    files = []
    if not GOLDEN_DIR.exists():
        logger.warning(f"Golden directory not found: {GOLDEN_DIR}")
        return files

    for connector_dir in sorted(GOLDEN_DIR.iterdir()):
        if not connector_dir.is_dir():
            continue
        connector = connector_dir.name
        for file_path in sorted(connector_dir.iterdir()):
            if file_path.is_file() and file_path.suffix in ('.java', '.js', '.xml', '.sql'):
                content = file_path.read_text(encoding='utf-8')
                files.append({
                    "path": file_path,
                    "file_name": file_path.name,
                    "content": content,
                    "connector": connector,
                    "is_golden": True,
                })
                logger.info(f"  Found Golden: {connector}/{file_path.name}")
    return files


# ── Framework Base Classes ──

FRAMEWORK_FILES = [
    "integration/components/src/main/java/com/saba/integration/http/HTTPComponentControl.java",
    "integration/components/src/main/java/com/saba/integration/http/authentication/AbstractReusableAuthStrategy.java",
    "marketplace/src/main/java/com/saba/integration/marketplace/account/test/VendorTestConnection.java",
]

def collect_framework_classes() -> List[Dict[str, Any]]:
    """Collect framework base class source files."""
    files = []
    for rel_path in FRAMEWORK_FILES:
        path = SIH_BASE / rel_path
        if path.exists():
            content = path.read_text(encoding='utf-8')
            files.append({
                "path": path,
                "file_name": path.name,
                "content": content,
                "connector": "_framework",
                "is_golden": False,
            })
            logger.info(f"  Found Framework: {path.name}")
        else:
            logger.warning(f"  Missing Framework: {path}")
    return files


# ── Framework Rules ──

RULES_FILE = BUILDER_BASE / "knowledge" / "framework_rules.md"
ARCH_GUIDE = BUILDER_BASE / "docs" / "nova_connector_architecture_guide.md"

def collect_framework_docs() -> List[Dict[str, Any]]:
    """Collect framework rules and architecture documentation."""
    files = []
    for path in [RULES_FILE, ARCH_GUIDE]:
        if path.exists():
            content = path.read_text(encoding='utf-8')
            files.append({
                "path": path,
                "file_name": path.name,
                "content": content,
                "connector": "_framework",
                "is_golden": False,
            })
            logger.info(f"  Found Doc: {path.name}")
    return files


# ── API Docs Upload ──

API_DOCS_DIR = BUILDER_BASE / "knowledge" / "api_docs"

def collect_uploaded_api_docs() -> List[Dict[str, Any]]:
    """Collect user-uploaded API documentation files."""
    files = []
    if not API_DOCS_DIR.exists():
        return files

    for file_path in sorted(API_DOCS_DIR.rglob("*")):
        if file_path.is_file() and file_path.suffix in ('.json', '.yaml', '.yml', '.md', '.txt'):
            content = file_path.read_text(encoding='utf-8')
            # Derive connector name from parent dir or filename
            connector = file_path.parent.name if file_path.parent != API_DOCS_DIR else file_path.stem.split('_')[0]
            files.append({
                "path": file_path,
                "file_name": file_path.name,
                "content": content,
                "connector": connector,
                "is_golden": False,
            })
            logger.info(f"  Found API Doc: {connector}/{file_path.name}")
    return files


# ── Main Ingestion ──

def ingest_framework(
    embedder: EmbeddingService = None,
    store: VectorStore = None,
    reset: bool = False,
):
    """
    Ingest golden references, framework base classes, and docs into ChromaDB.
    This should be run BEFORE or alongside connector code ingestion.
    """
    if embedder is None:
        embedder = EmbeddingService()
    if store is None:
        store = VectorStore()

    if reset:
        logger.info("Resetting all collections for full re-ingestion...")
        from vectordb.store import ALL_COLLECTIONS
        for coll_name in ALL_COLLECTIONS:
            try:
                store.delete_collection(coll_name)
            except Exception as e:
                logger.warning(f"Could not delete {coll_name}: {e}")

    # ── 1. Golden References → connector_knowledge (with golden=true metadata) ──
    golden_files = collect_golden_references()
    golden_chunks = []
    for file_info in golden_files:
        # For golden files, keep as WHOLE FILE — never split by method
        chunk = {
            "text": file_info["content"],
            "metadata": {
                "connector": file_info["connector"],
                "component_type": _detect_type(file_info["file_name"]),
                "language": _detect_lang(file_info["file_name"]),
                "file_name": file_info["file_name"],
                "chunk_part": "full_file",
                "is_golden": "true",
                "auth_type": "",
                "entity_type": "",
            },
            "id": f"golden_{file_info['connector']}_{file_info['file_name']}",
        }
        golden_chunks.append(chunk)

    if golden_chunks:
        logger.info(f"\nIndexing {len(golden_chunks)} golden reference chunks...")
        texts = [c["text"] for c in golden_chunks]
        metadatas = [c["metadata"] for c in golden_chunks]
        ids = [c["id"] for c in golden_chunks]
        embeddings = embedder.embed_texts(texts)
        store.add_documents(
            collection_name=CONNECTOR_KNOWLEDGE,
            documents=texts,
            embeddings=embeddings,
            metadatas=metadatas,
            ids=ids,
        )

    # ── 2. Framework Base Classes → nova_framework ──
    framework_files = collect_framework_classes()
    framework_chunks = []
    for file_info in framework_files:
        # Base classes as whole files
        chunk = {
            "text": file_info["content"],
            "metadata": {
                "connector": "_framework",
                "component_type": "base_class",
                "language": "java",
                "file_name": file_info["file_name"],
                "chunk_part": "full_file",
                "is_golden": "false",
                "auth_type": "",
                "entity_type": "",
            },
            "id": f"framework_{file_info['file_name']}",
        }
        framework_chunks.append(chunk)

    # ── 3. Framework Rules + Arch Guide → nova_framework ──
    doc_files = collect_framework_docs()
    for file_info in doc_files:
        md_chunks = chunk_markdown_file(
            file_info["content"], file_info["file_name"], "_framework"
        )
        for i, mc in enumerate(md_chunks):
            mc["id"] = f"doc_{file_info['file_name']}_{i}"
            mc["metadata"]["is_golden"] = "false"
        framework_chunks.extend(md_chunks)

    if framework_chunks:
        logger.info(f"\nIndexing {len(framework_chunks)} framework chunks...")
        texts = [c["text"] for c in framework_chunks]
        metadatas = [c["metadata"] for c in framework_chunks]
        ids = [c["id"] for c in framework_chunks]
        embeddings = embedder.embed_texts(texts)
        store.add_documents(
            collection_name=NOVA_FRAMEWORK,
            documents=texts,
            embeddings=embeddings,
            metadatas=metadatas,
            ids=ids,
        )

    # ── 4. API Docs → api_docs ──
    from vectordb.store import API_DOCS
    api_files = collect_uploaded_api_docs()
    api_chunks = []
    for file_info in api_files:
        if file_info["file_name"].endswith('.md'):
            chunks = chunk_markdown_file(
                file_info["content"], file_info["file_name"], file_info["connector"]
            )
        else:
            # JSON/YAML: whole file as one chunk
            chunks = [{
                "text": file_info["content"],
                "metadata": {
                    "connector": file_info["connector"],
                    "component_type": "api_documentation",
                    "language": _detect_lang(file_info["file_name"]),
                    "file_name": file_info["file_name"],
                    "chunk_part": "full_file",
                    "is_golden": "false",
                    "auth_type": "",
                    "entity_type": "",
                },
            }]
        for i, c in enumerate(chunks):
            c["id"] = f"api_{file_info['connector']}_{file_info['file_name']}_{i}"
        api_chunks.extend(chunks)

    if api_chunks:
        logger.info(f"\nIndexing {len(api_chunks)} API doc chunks...")
        texts = [c["text"] for c in api_chunks]
        metadatas = [c["metadata"] for c in api_chunks]
        ids = [c["id"] for c in api_chunks]
        embeddings = embedder.embed_texts(texts)
        store.add_documents(
            collection_name=API_DOCS,
            documents=texts,
            embeddings=embeddings,
            metadatas=metadatas,
            ids=ids,
        )

    stats = store.get_stats()
    logger.info(f"\n{'='*50}")
    logger.info("Framework ingestion complete!")
    for name, count in stats.items():
        logger.info(f"  {name}: {count} documents")
    logger.info(f"{'='*50}")
    return stats


def _detect_type(file_name: str) -> str:
    """Detect component type from filename."""
    name = file_name.lower()
    if "constants" in name: return "constants"
    if "testconnection" in name: return "test_connection"
    if "componentcontrol" in name: return "component_control"
    if "flows" in name: return "flows"
    if "dmlsetup" in name or "dml_setup" in name: return "dml_setup"
    if name.endswith(".js"): return "flow_definition"
    if name.endswith(".xml"): return "mapping"
    if name.endswith(".sql"): return "dml"
    return "other"


def _detect_lang(file_name: str) -> str:
    """Detect language from extension."""
    ext = Path(file_name).suffix.lower()
    return {".java": "java", ".js": "javascript", ".xml": "xml",
            ".sql": "sql", ".md": "markdown", ".json": "json",
            ".yaml": "yaml", ".yml": "yaml"}.get(ext, "text")


if __name__ == "__main__":
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    )
    import argparse
    parser = argparse.ArgumentParser(description="Ingest framework knowledge")
    parser.add_argument("--reset", action="store_true")
    args = parser.parse_args()
    ingest_framework(reset=args.reset)

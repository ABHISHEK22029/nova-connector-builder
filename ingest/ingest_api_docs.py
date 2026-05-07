"""
ingest/ingest_api_docs.py
Hot-swappable ingestion of target API documentation.
Drop docs into knowledge/api_docs/ and run this per connector.
"""

import os
import sys
import logging
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from dotenv import load_dotenv

load_dotenv()

from ingest.chunker import chunk_file
from embeddings.embedder import EmbeddingService
from vectordb.store import VectorStore, API_DOCS

logger = logging.getLogger(__name__)

API_DOCS_DIR = Path(__file__).resolve().parent.parent / "knowledge" / "api_docs"


def ingest_api_docs(
    connector_name: str,
    embedder: EmbeddingService = None,
    store: VectorStore = None,
    reset: bool = True,
):
    """
    Ingest API documentation files for a target connector.
    Supports: .md, .txt, .json, .yaml files
    """
    if embedder is None:
        embedder = EmbeddingService()
    if store is None:
        store = VectorStore()

    if reset:
        store.delete_collection(API_DOCS)

    docs_dir = API_DOCS_DIR / connector_name
    if not docs_dir.exists():
        # Also check flat files with connector name prefix
        docs_dir = API_DOCS_DIR
        logger.info(f"Looking for {connector_name} docs in: {docs_dir}")

    all_chunks = []
    supported_exts = {".md", ".txt", ".json", ".yaml", ".yml", ".html"}

    for doc_file in sorted(docs_dir.iterdir()):
        if doc_file.suffix.lower() not in supported_exts:
            continue
        if connector_name.lower() not in doc_file.name.lower() and docs_dir != API_DOCS_DIR / connector_name:
            continue

        logger.info(f"  Ingesting API doc: {doc_file.name}")
        content = doc_file.read_text(encoding="utf-8", errors="replace")

        chunks = chunk_file(
            content=content,
            file_name=doc_file.name,
            connector=connector_name,
        )

        # Tag all chunks as api_docs
        for chunk in chunks:
            chunk["metadata"]["component_type"] = "api_documentation"

        all_chunks.extend(chunks)

    if not all_chunks:
        logger.warning(f"No API docs found for '{connector_name}' in {docs_dir}")
        return

    logger.info(f"Generating embeddings for {len(all_chunks)} API doc chunks...")
    texts = [c["text"] for c in all_chunks]
    metadatas = [c["metadata"] for c in all_chunks]
    ids = [c["id"] for c in all_chunks]

    embeddings = embedder.embed_texts(texts)

    store.add_documents(
        collection_name=API_DOCS,
        documents=texts,
        embeddings=embeddings,
        metadatas=metadatas,
        ids=ids,
    )

    logger.info(f"Ingested {len(all_chunks)} API doc chunks for '{connector_name}'")


def ingest_nova_docs(
    embedder: EmbeddingService = None,
    store: VectorStore = None,
    reset: bool = True,
):
    """Ingest Nova framework documentation."""
    from vectordb.store import NOVA_FRAMEWORK

    if embedder is None:
        embedder = EmbeddingService()
    if store is None:
        store = VectorStore()

    if reset:
        store.delete_collection(NOVA_FRAMEWORK)

    nova_docs_dir = Path(__file__).resolve().parent.parent / "knowledge" / "nova_docs"

    # Also check architecture guides and Kaltura documentation
    extra_dirs = [
        Path(__file__).resolve().parent.parent / "docs",  # Architecture guides
        Path("/Users/abhishekgupta1/Documents/Kaltura_Documentation"),
    ]

    all_chunks = []

    for search_dir in [nova_docs_dir] + extra_dirs:
        if not search_dir.exists():
            continue
        for doc_file in sorted(search_dir.rglob("*.md")):
            logger.info(f"  Ingesting Nova doc: {doc_file.name}")
            content = doc_file.read_text(encoding="utf-8", errors="replace")
            chunks = chunk_file(
                content=content,
                file_name=doc_file.name,
                connector="_nova_framework",
            )
            for chunk in chunks:
                chunk["metadata"]["component_type"] = "framework_documentation"
            all_chunks.extend(chunks)

    if not all_chunks:
        logger.warning("No Nova framework docs found")
        return

    logger.info(f"Generating embeddings for {len(all_chunks)} Nova doc chunks...")
    texts = [c["text"] for c in all_chunks]
    metadatas = [c["metadata"] for c in all_chunks]
    ids = [c["id"] for c in all_chunks]

    embeddings = embedder.embed_texts(texts)

    store.add_documents(
        collection_name=NOVA_FRAMEWORK,
        documents=texts,
        embeddings=embeddings,
        metadatas=metadatas,
        ids=ids,
    )

    logger.info(f"Ingested {len(all_chunks)} Nova framework doc chunks")


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")

    import argparse

    parser = argparse.ArgumentParser(description="Ingest API docs for a target connector")
    parser.add_argument("--connector", required=True, help="Target connector name (e.g., stripe)")
    parser.add_argument("--nova-docs", action="store_true", help="Also ingest Nova framework docs")
    args = parser.parse_args()

    embedder = EmbeddingService()
    store = VectorStore()

    ingest_api_docs(args.connector, embedder, store)

    if args.nova_docs:
        ingest_nova_docs(embedder, store)

"""
ingest/ingest_connectors.py
Walks the SIH codebase and indexes all existing connector files into ChromaDB.
This is the primary knowledge base for the RAG system.
"""

import os
import sys
import logging
from pathlib import Path
from typing import List, Dict, Any

# Add parent to path for imports
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from dotenv import load_dotenv

load_dotenv()

from ingest.chunker import chunk_file
from embeddings.embedder import EmbeddingService
from vectordb.store import VectorStore, CONNECTOR_KNOWLEDGE, DATA_SCHEMA

logger = logging.getLogger(__name__)

# SIH codebase directory structure
SIH_BASE = os.getenv("SIH_CODEBASE_PATH", "../")

# Connector directories to scan
JAVA_CONNECTORS_PATH = "integration/apps/src/main/java/com/saba/integration/apps"
FLOW_RESOURCES_PATH = "integration/apps/src/main/resources/com/saba/mapping"
DEFAULT_DATA_PATH = "integration/apps/src/main/resources/com/saba/mapping/default_data"
VENDOR_CONSTANTS_PATH = "marketplace/src/main/java/com/saba/integration/marketplace/vendor/VendorConstants.java"
DEFAULT_MAPPING_CONFIG_PATH = "marketplace/src/main/java/com/saba/integration/marketplace/mapping/util/DefaultMappingConfig.java"
DML_PATH = "database/dml3.4.0.sql"

# Connectors to prioritize for ingestion
PRIORITY_CONNECTORS = [
    "kaltura", "udemy", "lil", "percipio", "workday",
    "cornerstone", "ccc", "webex", "zoom", "credly",
    "smartrecruiters", "adobeconnect",
]


def _resolve_path(relative: str) -> Path:
    """Resolve a path relative to the SIH codebase."""
    return Path(SIH_BASE).resolve() / relative


def _read_file(path: Path) -> str:
    """Read file content, handling encoding issues."""
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return path.read_text(encoding="latin-1")


def collect_connector_files(connector_name: str) -> List[Dict[str, Any]]:
    """
    Collect all files belonging to a specific connector.
    Returns list of {"path": Path, "content": str, "connector": str}
    """
    files = []

    # 1. Java source files
    java_dir = _resolve_path(JAVA_CONNECTORS_PATH) / connector_name
    if java_dir.exists():
        for java_file in java_dir.rglob("*.java"):
            content = _read_file(java_file)
            files.append({
                "path": java_file,
                "file_name": java_file.name,
                "content": content,
                "connector": connector_name,
            })
            logger.info(f"  Found Java: {java_file.name}")

    # 2. JS flow definitions
    flow_dir = _resolve_path(FLOW_RESOURCES_PATH) / connector_name / "flow"
    if flow_dir.exists():
        for js_file in flow_dir.glob("*.js"):
            content = _read_file(js_file)
            files.append({
                "path": js_file,
                "file_name": js_file.name,
                "content": content,
                "connector": connector_name,
            })
            logger.info(f"  Found Flow: {js_file.name}")

    # 3. Also check for connector-specific resources outside /flow/
    # Some connectors have resources at the connector-name level
    resource_dir = _resolve_path(FLOW_RESOURCES_PATH) / connector_name
    if resource_dir.exists():
        for res_file in resource_dir.glob("*.xml"):
            content = _read_file(res_file)
            files.append({
                "path": res_file,
                "file_name": res_file.name,
                "content": content,
                "connector": connector_name,
            })

    return files


def collect_mapping_xml_files(connector_filter: str = None) -> List[Dict[str, Any]]:
    """
    Collect XML mapping files from default_data/.
    Optionally filter by connector name prefix.
    """
    files = []
    xml_dir = _resolve_path(DEFAULT_DATA_PATH)

    if not xml_dir.exists():
        logger.warning(f"Default data directory not found: {xml_dir}")
        return files

    for xml_file in sorted(xml_dir.glob("*.xml")):
        name_lower = xml_file.name.lower()

        # Detect which connector this mapping belongs to
        connector = "unknown"
        for c in PRIORITY_CONNECTORS:
            if c in name_lower:
                connector = c
                break
        # Check broader names
        if connector == "unknown":
            if "linkedin" in name_lower:
                connector = "lil"
            elif "evolve_saba" in name_lower:
                connector = "saba_evolve"
            elif "evolve_sumtotal" in name_lower:
                connector = "sumtotal_evolve"
            elif "evolve_talentspace" in name_lower:
                connector = "talentspace_evolve"
            elif "sbx_" in name_lower:
                connector = "saba_cloud"
            elif "generic_sftp" in name_lower:
                connector = "generic_sftp"

        if connector_filter and connector != connector_filter:
            continue

        content = _read_file(xml_file)
        files.append({
            "path": xml_file,
            "file_name": xml_file.name,
            "content": content,
            "connector": connector,
        })

    logger.info(f"Found {len(files)} XML mapping files")
    return files


def collect_schema_files() -> List[Dict[str, Any]]:
    """Collect VendorConstants, DefaultMappingConfig, and DML."""
    files = []

    for rel_path, label in [
        (VENDOR_CONSTANTS_PATH, "VendorConstants"),
        (DEFAULT_MAPPING_CONFIG_PATH, "DefaultMappingConfig"),
    ]:
        path = _resolve_path(rel_path)
        if path.exists():
            content = _read_file(path)
            files.append({
                "path": path,
                "file_name": path.name,
                "content": content,
                "connector": "_schema",
            })
            logger.info(f"  Found Schema: {label}")

    # DML — split into manageable sections
    dml_path = _resolve_path(DML_PATH)
    if dml_path.exists():
        content = _read_file(dml_path)
        # Only take first 100K chars to avoid memory issues
        files.append({
            "path": dml_path,
            "file_name": dml_path.name,
            "content": content[:100000],
            "connector": "_schema",
        })
        logger.info(f"  Found DML: {dml_path.name}")

    return files


def ingest_all(
    embedder: EmbeddingService = None,
    store: VectorStore = None,
    connectors: List[str] = None,
    reset: bool = False,
):
    """
    Main ingestion pipeline. Indexes connector code + schema into ChromaDB.
    """
    if embedder is None:
        embedder = EmbeddingService()
    if store is None:
        store = VectorStore()

    target_connectors = connectors or PRIORITY_CONNECTORS

    if reset:
        logger.info("Resetting collections...")
        store.delete_collection(CONNECTOR_KNOWLEDGE)
        store.delete_collection(DATA_SCHEMA)

    # ── 1. Ingest connector code ──
    all_chunks = []
    for connector in target_connectors:
        logger.info(f"\n{'='*50}")
        logger.info(f"Processing connector: {connector}")
        logger.info(f"{'='*50}")

        files = collect_connector_files(connector)
        for file_info in files:
            chunks = chunk_file(
                content=file_info["content"],
                file_name=file_info["file_name"],
                connector=file_info["connector"],
            )
            all_chunks.extend(chunks)

    # Also ingest connector-specific XML mappings
    xml_files = collect_mapping_xml_files()
    for file_info in xml_files:
        chunks = chunk_file(
            content=file_info["content"],
            file_name=file_info["file_name"],
            connector=file_info["connector"],
        )
        all_chunks.extend(chunks)

    if all_chunks:
        logger.info(f"\nGenerating embeddings for {len(all_chunks)} connector chunks...")
        texts = [c["text"] for c in all_chunks]
        metadatas = [c["metadata"] for c in all_chunks]
        ids = [c["id"] for c in all_chunks]

        embeddings = embedder.embed_texts(texts)

        store.add_documents(
            collection_name=CONNECTOR_KNOWLEDGE,
            documents=texts,
            embeddings=embeddings,
            metadatas=metadatas,
            ids=ids,
        )

    # ── 2. Ingest schema files ──
    schema_files = collect_schema_files()
    schema_chunks = []
    for file_info in schema_files:
        chunks = chunk_file(
            content=file_info["content"],
            file_name=file_info["file_name"],
            connector=file_info["connector"],
        )
        schema_chunks.extend(chunks)

    if schema_chunks:
        logger.info(f"\nGenerating embeddings for {len(schema_chunks)} schema chunks...")
        texts = [c["text"] for c in schema_chunks]
        metadatas = [c["metadata"] for c in schema_chunks]
        ids = [c["id"] for c in schema_chunks]

        embeddings = embedder.embed_texts(texts)

        store.add_documents(
            collection_name=DATA_SCHEMA,
            documents=texts,
            embeddings=embeddings,
            metadatas=metadatas,
            ids=ids,
        )

    # ── Summary ──
    stats = store.get_stats()
    logger.info(f"\n{'='*50}")
    logger.info("Ingestion complete!")
    for name, count in stats.items():
        logger.info(f"  {name}: {count} documents")
    logger.info(f"{'='*50}")

    return stats


# ---------------------------------------------------------------------------
# CLI entry point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    )

    import argparse

    parser = argparse.ArgumentParser(description="Ingest connector knowledge into ChromaDB")
    parser.add_argument(
        "--connectors",
        nargs="*",
        default=None,
        help="Specific connectors to ingest (default: all priority connectors)",
    )
    parser.add_argument(
        "--reset",
        action="store_true",
        help="Reset collections before ingesting",
    )
    args = parser.parse_args()

    ingest_all(connectors=args.connectors, reset=args.reset)

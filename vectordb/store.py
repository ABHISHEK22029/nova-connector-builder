"""
vectordb/store.py
ChromaDB vector store with 4 collections for connector knowledge.
Supports cosine similarity search with metadata filtering.
"""

import os
import logging
from typing import List, Dict, Optional, Any

import chromadb
from chromadb.config import Settings

logger = logging.getLogger(__name__)

# Collection names
CONNECTOR_KNOWLEDGE = "connector_knowledge"
NOVA_FRAMEWORK = "nova_framework"
DATA_SCHEMA = "data_schema"
API_DOCS = "api_docs"
GENERATED_CONNECTORS = "generated_connectors"

ALL_COLLECTIONS = [
    CONNECTOR_KNOWLEDGE,
    NOVA_FRAMEWORK,
    DATA_SCHEMA,
    API_DOCS,
    GENERATED_CONNECTORS,
]


class VectorStore:
    """ChromaDB-backed vector store with multiple collections."""

    def __init__(self, persist_dir: str = None):
        self.persist_dir = persist_dir or os.getenv("CHROMA_PERSIST_DIR", "./chroma_db")
        logger.info(f"Initializing ChromaDB at: {self.persist_dir}")

        self._client = chromadb.PersistentClient(
            path=self.persist_dir,
            settings=Settings(anonymized_telemetry=False),
        )

        # Initialize all collections
        self._collections = {}
        for name in ALL_COLLECTIONS:
            self._collections[name] = self._client.get_or_create_collection(
                name=name,
                metadata={"hnsw:space": "cosine"},
            )
            count = self._collections[name].count()
            logger.info(f"  Collection '{name}': {count} documents")

    def get_collection(self, name: str):
        """Get a ChromaDB collection by name."""
        if name not in self._collections:
            raise ValueError(f"Unknown collection: {name}. Valid: {ALL_COLLECTIONS}")
        return self._collections[name]

    def add_documents(
        self,
        collection_name: str,
        documents: List[str],
        embeddings: List[List[float]],
        metadatas: List[Dict[str, Any]],
        ids: List[str],
    ) -> int:
        """
        Add documents with embeddings and metadata to a collection.
        Returns the number of documents added.
        """
        collection = self.get_collection(collection_name)

        # ChromaDB add in batches (max ~41666 per batch for large embeddings)
        batch_size = 500
        added = 0
        for i in range(0, len(documents), batch_size):
            batch_end = min(i + batch_size, len(documents))
            collection.add(
                documents=documents[i:batch_end],
                embeddings=embeddings[i:batch_end],
                metadatas=metadatas[i:batch_end],
                ids=ids[i:batch_end],
            )
            added += batch_end - i

        logger.info(f"Added {added} documents to '{collection_name}'")
        return added

    def query(
        self,
        collection_name: str,
        query_embedding: List[float],
        top_k: int = 5,
        where: Optional[Dict] = None,
        where_document: Optional[Dict] = None,
    ) -> Dict:
        """
        Similarity search against a collection.
        Returns {"ids", "documents", "metadatas", "distances"}.
        """
        collection = self.get_collection(collection_name)

        kwargs = {
            "query_embeddings": [query_embedding],
            "n_results": min(top_k, collection.count()) if collection.count() > 0 else 1,
            "include": ["documents", "metadatas", "distances"],
        }

        if where:
            kwargs["where"] = where
        if where_document:
            kwargs["where_document"] = where_document

        try:
            results = collection.query(**kwargs)
        except Exception as e:
            logger.warning(f"Query failed on '{collection_name}': {e}")
            return {"ids": [[]], "documents": [[]], "metadatas": [[]], "distances": [[]]}

        return results

    def delete_collection(self, collection_name: str):
        """Delete and recreate a collection (for re-ingestion)."""
        self._client.delete_collection(collection_name)
        self._collections[collection_name] = self._client.get_or_create_collection(
            name=collection_name,
            metadata={"hnsw:space": "cosine"},
        )
        logger.info(f"Collection '{collection_name}' reset")

    def get_stats(self) -> Dict[str, int]:
        """Return document counts for all collections."""
        return {name: col.count() for name, col in self._collections.items()}

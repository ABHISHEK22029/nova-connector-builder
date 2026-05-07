"""
embeddings/embedder.py
Embedding generation using BAAI/bge-large-en via sentence-transformers.
Generates normalized embeddings for cosine similarity search.
"""

import os
import logging
from typing import List

from sentence_transformers import SentenceTransformer
import numpy as np

logger = logging.getLogger(__name__)

# Default model — can be overridden via .env
DEFAULT_MODEL = "BAAI/bge-large-en"


class EmbeddingService:
    """Generates embeddings using a HuggingFace sentence-transformer model."""

    def __init__(self, model_name: str = None):
        self.model_name = model_name or os.getenv("EMBEDDING_MODEL", DEFAULT_MODEL)
        logger.info(f"Loading embedding model: {self.model_name}")
        self._model = SentenceTransformer(self.model_name)
        logger.info(f"Model loaded. Embedding dimension: {self.get_dimension()}")

    def get_dimension(self) -> int:
        """Return the dimensionality of the embeddings."""
        return self._model.get_sentence_embedding_dimension()

    def embed_texts(self, texts: List[str], batch_size: int = 32) -> List[List[float]]:
        """
        Generate normalized embeddings for a list of texts.
        Returns list of float arrays suitable for ChromaDB.
        """
        if not texts:
            return []

        # BGE models recommend prepending "Represent this sentence: " for retrieval
        if "bge" in self.model_name.lower():
            texts = [f"Represent this sentence: {t}" for t in texts]

        embeddings = self._model.encode(
            texts,
            batch_size=batch_size,
            show_progress_bar=True,
            normalize_embeddings=True,  # L2 normalize for cosine similarity
        )

        return embeddings.tolist()

    def embed_query(self, query: str) -> List[float]:
        """
        Generate a single embedding for a search query.
        BGE models use a different prefix for queries vs documents.
        """
        if "bge" in self.model_name.lower():
            query = f"Represent this sentence for searching relevant passages: {query}"

        embedding = self._model.encode(
            [query],
            normalize_embeddings=True,
        )

        return embedding[0].tolist()

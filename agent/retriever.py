"""
agent/retriever.py
Retrieval engine — golden-first, full-file, auth-matched similarity search.
OVERHAUL: Prioritizes golden references and always includes framework rules.
"""

import logging
from typing import List, Dict, Any, Optional

from embeddings.embedder import EmbeddingService
from vectordb.store import (
    VectorStore,
    CONNECTOR_KNOWLEDGE,
    NOVA_FRAMEWORK,
    DATA_SCHEMA,
    API_DOCS,
)

logger = logging.getLogger(__name__)


def _flatten_results(results: Dict) -> List[Dict[str, Any]]:
    """Flatten ChromaDB query results into a list of dicts."""
    flattened = []
    if not results or not results.get("documents"):
        return flattened

    docs = results["documents"][0]
    metas = results["metadatas"][0]
    dists = results["distances"][0]
    ids = results["ids"][0]

    for i in range(len(docs)):
        flattened.append({
            "id": ids[i],
            "text": docs[i],
            "metadata": metas[i],
            "distance": dists[i],
            "score": 1.0 - dists[i],
        })

    return flattened


class RetrievalEngine:
    """Retrieves relevant knowledge chunks with golden-first priority."""

    def __init__(self, embedder: EmbeddingService, store: VectorStore):
        self.embedder = embedder
        self.store = store

    def retrieve(
        self,
        query: str,
        top_k: int = 5,
        collections: List[str] = None,
        where: Optional[Dict] = None,
    ) -> List[Dict[str, Any]]:
        """Search across specified collections and return merged, ranked results."""
        if collections is None:
            collections = [CONNECTOR_KNOWLEDGE, DATA_SCHEMA, API_DOCS, NOVA_FRAMEWORK]

        query_embedding = self.embedder.embed_query(query)

        all_results = []
        for collection_name in collections:
            stats = self.store.get_stats()
            if stats.get(collection_name, 0) == 0:
                continue

            results = self.store.query(
                collection_name=collection_name,
                query_embedding=query_embedding,
                top_k=top_k,
                where=where,
            )

            flat = _flatten_results(results)
            for item in flat:
                item["collection"] = collection_name
            all_results.extend(flat)

        # Sort: golden first, then by score
        all_results.sort(key=lambda x: (
            x.get("metadata", {}).get("is_golden") == "true",  # Golden first
            x["score"]
        ), reverse=True)

        if all_results:
            logger.info(
                f"Retrieved {len(all_results)} results for query: "
                f"'{query[:60]}...' (top score: {all_results[0]['score']:.3f}, "
                f"golden: {sum(1 for r in all_results if r.get('metadata', {}).get('is_golden') == 'true')})"
            )
        else:
            logger.info(f"No results for: '{query[:60]}...'")

        return all_results

    def retrieve_for_file_task(
        self,
        file_type: str,
        connector_spec: Any,
        top_k: int = 8,
    ) -> List[Dict[str, Any]]:
        """
        Retrieve context for generating a specific file type.
        Uses targeted queries with golden-first priority and auth-matching.
        """
        queries_and_filters = self._build_queries_for_file(file_type, connector_spec)

        all_results = []
        seen_ids = set()

        # Step 1: Always get the golden reference for this file type
        golden_results = self._get_golden_for_file_type(file_type)
        for r in golden_results:
            if r["id"] not in seen_ids:
                seen_ids.add(r["id"])
                all_results.append(r)

        # Step 2: Always get framework rules
        framework_results = self._get_framework_rules(file_type)
        for r in framework_results:
            if r["id"] not in seen_ids:
                seen_ids.add(r["id"])
                all_results.append(r)

        # Step 3: Targeted retrieval queries
        for query, collections, where_filter in queries_and_filters:
            results = self.retrieve(
                query=query,
                top_k=top_k,
                collections=collections,
                where=where_filter,
            )
            for r in results:
                if r["id"] not in seen_ids:
                    seen_ids.add(r["id"])
                    all_results.append(r)

        # Re-sort: golden first, then framework, then by score
        all_results.sort(key=lambda x: (
            x.get("metadata", {}).get("is_golden") == "true",
            x.get("collection") == NOVA_FRAMEWORK,
            x["score"]
        ), reverse=True)

        return all_results[:top_k * 4]  # More generous budget

    def _get_golden_for_file_type(self, file_type: str) -> List[Dict[str, Any]]:
        """Get golden reference files matching the requested file type."""
        component_type_map = {
            "constants": "constants",
            "test_connection": "test_connection",
            "component_control": "component_control",
            "flows": "flows",
            "flow_definition": "flow_definition",
            "mapping_xml": "mapping",
        }

        comp_type = component_type_map.get(file_type)
        if not comp_type:
            return []

        stats = self.store.get_stats()
        if stats.get(CONNECTOR_KNOWLEDGE, 0) == 0:
            return []

        # Query for golden files of this component type
        query = f"golden reference {file_type} connector pattern"
        query_embedding = self.embedder.embed_query(query)

        try:
            results = self.store.query(
                collection_name=CONNECTOR_KNOWLEDGE,
                query_embedding=query_embedding,
                top_k=3,
                where={"$and": [
                    {"is_golden": "true"},
                    {"component_type": comp_type},
                ]},
            )
            flat = _flatten_results(results)
            for item in flat:
                item["collection"] = CONNECTOR_KNOWLEDGE
                # Boost golden scores
                item["score"] = min(item["score"] + 0.5, 1.0)
            return flat
        except Exception as e:
            logger.debug(f"Golden retrieval failed (expected on first run): {e}")
            return []

    def _get_framework_rules(self, file_type: str) -> List[Dict[str, Any]]:
        """Get relevant framework rules for this file type."""
        stats = self.store.get_stats()
        if stats.get(NOVA_FRAMEWORK, 0) == 0:
            return []

        rule_queries = {
            "constants": "constants string externalization no hardcoded values",
            "test_connection": "TestConnection delegates shared auth VendorTestConnection",
            "component_control": "ComponentControl minimal base class pagination",
            "flows": "Flows no Bean ComponentControl auth strategy registry",
            "flow_definition": "Content.js sabaspel pipeline HTTP jsonToXml preview",
            "mapping_xml": "XML mapping source target fields EdCast",
            "vendor_constants": "VendorConstants integration ID registration",
            "mapping_config": "DefaultMappingConfig enum mapping file",
            "dml": "DML SQL registration vendor entity association",
        }

        query = rule_queries.get(file_type, f"Nova framework {file_type} rules")
        query_embedding = self.embedder.embed_query(query)

        try:
            results = self.store.query(
                collection_name=NOVA_FRAMEWORK,
                query_embedding=query_embedding,
                top_k=5,
            )
            flat = _flatten_results(results)
            for item in flat:
                item["collection"] = NOVA_FRAMEWORK
            return flat
        except Exception as e:
            logger.debug(f"Framework rules retrieval failed: {e}")
            return []

    def _build_queries_for_file(
        self, file_type: str, spec: Any
    ) -> List[tuple]:
        """Build targeted query + filter combos for each file type."""
        name = spec.connector_name if hasattr(spec, "connector_name") else "connector"
        auth = spec.auth_type if hasattr(spec, "auth_type") else "unknown"

        file_queries = {
            "constants": [
                (
                    f"Java Constants class API configuration keys endpoints headers {auth}",
                    [CONNECTOR_KNOWLEDGE],
                    {"component_type": "constants"},
                ),
                (
                    f"{name} API endpoints authentication configuration",
                    [API_DOCS],
                    None,
                ),
            ],
            "test_connection": [
                (
                    f"TestConnection {auth} authentication validation credentials",
                    [CONNECTOR_KNOWLEDGE],
                    {"component_type": "test_connection"},
                ),
                (
                    f"{name} authentication API endpoint credentials validation",
                    [API_DOCS],
                    None,
                ),
            ],
            "component_control": [
                (
                    f"ComponentControl minimal override pagination header body {auth}",
                    [CONNECTOR_KNOWLEDGE],
                    {"component_type": "component_control"},
                ),
            ],
            "flows": [
                (
                    f"Flows Spring Configuration IntegrationFlow bean {auth} authentication strategy",
                    [CONNECTOR_KNOWLEDGE],
                    {"component_type": "flows"},
                ),
            ],
            "flow_definition": [
                (
                    f"Content.js flow definition http jsonToXml xslt pipeline {auth} sabaspel",
                    [CONNECTOR_KNOWLEDGE],
                    {"component_type": "flow_definition"},
                ),
            ],
            "mapping_xml": [
                (
                    f"XML mapping source target attributes transformation content fields",
                    [CONNECTOR_KNOWLEDGE],
                    {"component_type": "mapping"},
                ),
                (
                    f"{name} API response fields data schema content listing",
                    [API_DOCS],
                    None,
                ),
            ],
            "vendor_constants": [
                (
                    f"VendorConstants integration ID registration",
                    [DATA_SCHEMA],
                    None,
                ),
            ],
            "mapping_config": [
                (
                    f"DefaultMappingConfig enum mapping file integration entity",
                    [DATA_SCHEMA],
                    None,
                ),
            ],
            "dml": [
                (
                    f"DML SQL mpp_vendor_entity_ins mpp_integration_entity_assoc_ins registration",
                    [DATA_SCHEMA],
                    None,
                ),
            ],
        }

        return file_queries.get(file_type, [
            (f"{name} connector {file_type}", [CONNECTOR_KNOWLEDGE, API_DOCS], None)
        ])

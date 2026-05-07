"""
agent/context_builder.py
Builds structured context from retrieved chunks for the LLM prompt.
OVERHAUL: 16K token budget, golden-first sections, never truncates golden refs.
"""

import logging
from typing import List, Dict, Any

logger = logging.getLogger(__name__)

# Increased from 6000 → 16000 for much richer context
MAX_CONTEXT_TOKENS = 16000
CHARS_PER_TOKEN = 4


def estimate_tokens(text: str) -> int:
    """Rough token estimate."""
    return len(text) // CHARS_PER_TOKEN


def build_context(
    retrieved_chunks: List[Dict[str, Any]],
    file_type: str,
    connector_name: str,
    max_tokens: int = MAX_CONTEXT_TOKENS,
) -> str:
    """
    Organize retrieved chunks into structured context.
    Priority: Golden Reference > Framework Rules > API Docs > Other Code > Schema
    """
    groups = {
        "golden_reference": [],
        "framework_rules": [],
        "reference_code": [],
        "api_docs": [],
        "schema_info": [],
    }

    for chunk in retrieved_chunks:
        collection = chunk.get("collection", "")
        is_golden = chunk.get("metadata", {}).get("is_golden") == "true"

        if is_golden:
            groups["golden_reference"].append(chunk)
        elif "nova_framework" in collection:
            groups["framework_rules"].append(chunk)
        elif "connector_knowledge" in collection:
            groups["reference_code"].append(chunk)
        elif "api_docs" in collection:
            groups["api_docs"].append(chunk)
        elif "data_schema" in collection:
            groups["schema_info"].append(chunk)
        else:
            groups["reference_code"].append(chunk)

    sections = []
    remaining_tokens = max_tokens

    # Priority 1: Golden Reference (50% budget, NEVER truncate)
    if groups["golden_reference"]:
        section = _build_section(
            "GOLDEN REFERENCE — Follow this pattern EXACTLY",
            groups["golden_reference"],
            int(remaining_tokens * 0.50),
            never_truncate=True,
        )
        if section:
            sections.append(section)
            remaining_tokens -= estimate_tokens(section)

    # Priority 2: Framework Rules (20% budget)
    if groups["framework_rules"]:
        section = _build_section(
            "FRAMEWORK RULES — These are NON-NEGOTIABLE constraints",
            groups["framework_rules"],
            int(remaining_tokens * 0.25),
        )
        if section:
            sections.append(section)
            remaining_tokens -= estimate_tokens(section)

    # Priority 3: API documentation (15% budget)
    if groups["api_docs"]:
        section = _build_section(
            f"TARGET API DOCUMENTATION ({connector_name})",
            groups["api_docs"],
            int(remaining_tokens * 0.30),
        )
        if section:
            sections.append(section)
            remaining_tokens -= estimate_tokens(section)

    # Priority 4: Additional reference code (10% budget)
    if groups["reference_code"]:
        section = _build_section(
            "ADDITIONAL REFERENCE CONNECTOR CODE",
            groups["reference_code"],
            int(remaining_tokens * 0.30),
        )
        if section:
            sections.append(section)
            remaining_tokens -= estimate_tokens(section)

    # Priority 5: Schema info (5% budget)
    if groups["schema_info"]:
        section = _build_section(
            "DATA SCHEMA & REGISTRATION",
            groups["schema_info"],
            int(remaining_tokens * 0.20),
        )
        if section:
            sections.append(section)

    context = "\n\n".join(sections)

    logger.info(
        f"Built context for '{file_type}': "
        f"~{estimate_tokens(context)} tokens, "
        f"{len(retrieved_chunks)} chunks "
        f"(golden: {len(groups['golden_reference'])}, "
        f"rules: {len(groups['framework_rules'])}, "
        f"code: {len(groups['reference_code'])}, "
        f"api: {len(groups['api_docs'])})"
    )

    return context


def _build_section(
    heading: str,
    chunks: List[Dict[str, Any]],
    max_tokens: int,
    never_truncate: bool = False,
) -> str:
    """Build a single context section with token limiting."""
    lines = [f"## {heading}"]
    current_tokens = estimate_tokens(heading)

    seen_texts = set()

    for chunk in chunks:
        text = chunk["text"]
        text_key = text[:200]
        if text_key in seen_texts:
            continue
        seen_texts.add(text_key)

        meta = chunk.get("metadata", {})
        file_name = meta.get("file_name", "unknown")
        connector = meta.get("connector", "unknown")
        comp_type = meta.get("component_type", "")
        is_golden = meta.get("is_golden") == "true"

        label = "🏆 GOLDEN" if is_golden else "📄"
        chunk_header = f"\n### {label} {file_name} (connector: {connector}, type: {comp_type})"
        chunk_content = text

        chunk_tokens = estimate_tokens(chunk_header + chunk_content)

        if not never_truncate and current_tokens + chunk_tokens > max_tokens:
            remaining = max_tokens - current_tokens - estimate_tokens(chunk_header)
            if remaining > 200:
                chunk_content = text[: remaining * CHARS_PER_TOKEN] + "\n... [truncated]"
            else:
                break

        lines.append(chunk_header)
        lines.append(f"```\n{chunk_content}\n```")
        current_tokens += chunk_tokens

    if len(lines) <= 1:
        return ""

    return "\n".join(lines)

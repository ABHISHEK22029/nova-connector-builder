"""
ingest/chunker.py
Semantic chunking engine that splits connector files by their logical role.
Different strategies for Java, JavaScript flow, XML mapping, and SQL DML files.
"""

import re
import logging
import hashlib
from typing import List, Dict, Any, Tuple
from pathlib import Path

logger = logging.getLogger(__name__)


def _generate_chunk_id(connector: str, file_name: str, chunk_index: int) -> str:
    """Generate a deterministic chunk ID for deduplication."""
    raw = f"{connector}:{file_name}:{chunk_index}"
    return hashlib.md5(raw.encode()).hexdigest()


def _detect_auth_type(content: str) -> str:
    """Heuristic to detect authentication type from code content."""
    content_lower = content.lower()
    if "oauth" in content_lower or "access_token" in content_lower:
        return "oauth2"
    if "basicauth" in content_lower or "basic_auth" in content_lower:
        return "basic_auth"
    if "api_key" in content_lower or "apikey" in content_lower:
        return "api_key"
    if "session" in content_lower and ("ks" in content_lower or "token" in content_lower):
        return "session_token"
    if "bearer" in content_lower:
        return "bearer_token"
    return "unknown"


def _detect_component_type(file_name: str, content: str) -> str:
    """Detect the component type from file name and content."""
    name_lower = file_name.lower()
    if "constants" in name_lower:
        return "constants"
    if "testconnection" in name_lower:
        return "test_connection"
    if "componentcontrol" in name_lower:
        return "component_control"
    if "flows" in name_lower:
        return "flows"
    if name_lower.endswith(".js"):
        return "flow_definition"
    if name_lower.endswith(".xml"):
        return "mapping"
    if name_lower.endswith(".sql"):
        return "dml"
    if "vendorconstants" in name_lower:
        return "vendor_constants"
    if "defaultmappingconfig" in name_lower:
        return "mapping_config"
    return "other"


def _detect_language(file_name: str) -> str:
    """Detect the programming language from file extension."""
    ext = Path(file_name).suffix.lower()
    return {
        ".java": "java",
        ".js": "javascript",
        ".xml": "xml",
        ".sql": "sql",
        ".md": "markdown",
        ".json": "json",
    }.get(ext, "text")


# ---------------------------------------------------------------------------
# Chunking strategies per file type
# ---------------------------------------------------------------------------


def chunk_java_file(content: str, file_name: str, connector: str) -> List[Dict[str, Any]]:
    """
    Split Java files by class and method boundaries.
    Each method chunk INCLUDES the class header for self-contained context.
    """
    chunks = []
    lines = content.split("\n")

    # First: extract class header (package + imports + class declaration)
    header_lines = []
    body_start = 0
    class_found = False

    for i, line in enumerate(lines):
        if not class_found:
            header_lines.append(line)
            if re.match(r'\s*(public|private|protected)?\s*(static\s+)?(abstract\s+)?(class|interface|enum|record|@interface)\s+', line):
                class_found = True
                body_start = i + 1
                break

    header_text = "\n".join(header_lines) if header_lines else ""

    # Add a full-file chunk (for golden references and fallback), but only if not massively huge
    if len(content) < 25000:
        full_chunk = {
            "text": content,
            "metadata": {
                "connector": connector,
                "component_type": _detect_component_type(file_name, content),
                "auth_type": _detect_auth_type(content),
                "language": "java",
                "file_name": file_name,
                "chunk_part": "full_file",
                "entity_type": "",
            },
        }
        chunks.append(full_chunk)

    # Split remaining body by method signatures — each includes class header
    current_method = []
    class_body_remainder = []
    method_depth = 0
    in_method = False

    for i in range(body_start, len(lines)):
        line = lines[i]
        current_method.append(line)

        # Detect method start (improved regex for annotations and modifiers)
        if not in_method and re.match(
            r'\s*(@\w+\s+)*(public|private|protected)?\s+.*\(.*\)\s*(\{|throws)', line
        ):
            class_body_remainder.extend(current_method)
            current_method = []
            in_method = True
            method_depth = 0

        if in_method:
            method_depth += line.count("{") - line.count("}")
            if method_depth <= 0 and "{" in "\n".join(current_method):
                # Method complete — prepend class header for context
                method_text = header_text + "\n\n    // [METHOD CHUNK]\n" + "\n".join(current_method)
                chunks.append({
                    "text": method_text,
                    "metadata": {
                        "connector": connector,
                        "component_type": _detect_component_type(file_name, content),
                        "auth_type": _detect_auth_type(method_text),
                        "language": "java",
                        "file_name": file_name,
                        "chunk_part": "method_with_header",
                        "entity_type": "",
                    },
                })
                current_method = []
                in_method = False

    # Chunk anything remaining in the class body if it is large (e.g., massive enum declarations)
    if not in_method:
        class_body_remainder.extend(current_method)
    
    remainder_text = "\n".join(class_body_remainder).strip()
    if remainder_text and len(remainder_text) > 200:
        part = 0
        while len(remainder_text) > 0:
            chunk_text = header_text + "\n\n    // [CLASS REMAINDER]\n" + remainder_text[:12000]
            chunks.append({
                "text": chunk_text,
                "metadata": {
                    "connector": connector,
                    "component_type": _detect_component_type(file_name, content),
                    "auth_type": _detect_auth_type(chunk_text),
                    "language": "java",
                    "file_name": file_name,
                    "chunk_part": f"class_body_{part}",
                    "entity_type": "",
                },
            })
            remainder_text = remainder_text[12000:]
            part += 1

    return chunks


def chunk_js_flow_file(content: str, file_name: str, connector: str) -> List[Dict[str, Any]]:
    """
    Split JS flow definition files by component definitions and the flow routing section.
    Each component in the "components" array becomes a chunk, and the "flow" section is a chunk.
    """
    chunks = []

    # Always include the full file as one chunk for complete context
    chunks.append({
        "text": content,
        "metadata": {
            "connector": connector,
            "component_type": "flow_definition",
            "auth_type": _detect_auth_type(content),
            "language": "javascript",
            "file_name": file_name,
            "chunk_part": "full_file",
            "entity_type": _detect_entity_from_filename(file_name),
        },
    })

    # Also try to extract individual components
    try:
        import json
        data = json.loads(content)

        # Each component as a chunk
        for comp in data.get("components", []):
            comp_text = json.dumps(comp, indent=2)
            comp_type = comp.get("type", "unknown")
            comp_name = comp.get("name", "unknown")
            chunks.append({
                "text": f"// Component: {comp_name} (type: {comp_type})\n{comp_text}",
                "metadata": {
                    "connector": connector,
                    "component_type": f"flow_component_{comp_type}",
                    "auth_type": _detect_auth_type(comp_text),
                    "language": "javascript",
                    "file_name": file_name,
                    "chunk_part": f"component_{comp_type}",
                    "entity_type": _detect_entity_from_filename(file_name),
                },
            })

        # Flow routing as a chunk
        flow_section = data.get("flow", {})
        if flow_section:
            flow_text = json.dumps(flow_section, indent=2)
            chunks.append({
                "text": f"// Flow routing definition\n{flow_text}",
                "metadata": {
                    "connector": connector,
                    "component_type": "flow_routing",
                    "auth_type": "",
                    "language": "javascript",
                    "file_name": file_name,
                    "chunk_part": "flow_routing",
                    "entity_type": _detect_entity_from_filename(file_name),
                },
            })

    except (json.JSONDecodeError, Exception) as e:
        logger.debug(f"Could not parse JS as JSON for component splitting: {e}")

    return chunks


def chunk_xml_mapping_file(content: str, file_name: str, connector: str) -> List[Dict[str, Any]]:
    """
    Split XML mapping files into logical sections.
    For large files, split by top-level XML elements.
    """
    chunks = []
    entity_type = _detect_entity_from_filename(file_name)

    # Always include full file as one chunk (most XML files are < 4k tokens)
    if len(content) < 15000:
        chunks.append({
            "text": content,
            "metadata": {
                "connector": connector,
                "component_type": "mapping",
                "auth_type": "",
                "language": "xml",
                "file_name": file_name,
                "chunk_part": "full_file",
                "entity_type": entity_type,
            },
        })
    else:
        # For large XML files, split by major sections
        # Split on top-level element boundaries
        sections = re.split(r'(</[^>]+>)\s*\n\s*<', content)
        current_chunk = ""
        for section in sections:
            current_chunk += section
            if len(current_chunk) > 12000:
                chunks.append({
                    "text": current_chunk,
                    "metadata": {
                        "connector": connector,
                        "component_type": "mapping",
                        "auth_type": "",
                        "language": "xml",
                        "file_name": file_name,
                        "chunk_part": "section",
                        "entity_type": entity_type,
                    },
                })
                current_chunk = ""

        if current_chunk.strip():
            chunks.append({
                "text": current_chunk,
                "metadata": {
                    "connector": connector,
                    "component_type": "mapping",
                    "auth_type": "",
                    "language": "xml",
                    "file_name": file_name,
                    "chunk_part": "section",
                    "entity_type": entity_type,
                },
            })

    return chunks


def chunk_sql_file(content: str, file_name: str, connector: str) -> List[Dict[str, Any]]:
    """
    Split SQL DML files by procedure call blocks and comment-delimited sections.
    """
    chunks = []
    
    # Golden SQL files can be added entirely if small
    if len(content) < 25000 and "golden" in str(Path(file_name).absolute()):
        chunks.append({
            "text": content,
            "metadata": {
                "connector": connector,
                "component_type": "dml_setup",
                "auth_type": "",
                "language": "sql",
                "file_name": file_name,
                "chunk_part": "full_file",
                "entity_type": "",
            },
        })
        return chunks

    # Split by comment-delimited sections (e.g., -- ─── SECTION 1: PARTNER ───)
    # This matches >=3 hyphens, equals, or box-drawing characters
    sections = re.split(r'\n\s*--\s*[=─-]{3,}.*?\n', content)

    if len(sections) <= 1:
        # Try splitting by empty lines between procedure calls 
        sections = re.split(r'\n\s*\n\s*\n', content)

    for i, section in enumerate(sections):
        section = section.strip()
        if len(section) < 50:
            continue
            
        # If a section is still too large, split it further to avoid DB bloat
        while len(section) > 0:
            chunks.append({
                "text": section[:12000],
                "metadata": {
                    "connector": connector,
                    "component_type": _detect_component_type(file_name, content),
                    "auth_type": "",
                    "language": "sql",
                    "file_name": file_name,
                    "chunk_part": f"section_{i}",
                    "entity_type": "",
                },
            })
            section = section[12000:]

    return chunks


def chunk_markdown_file(content: str, file_name: str, connector: str) -> List[Dict[str, Any]]:
    """Split markdown files by heading sections."""
    chunks = []
    sections = re.split(r'\n(#{1,3}\s+)', content)

    current_heading = ""
    current_text = ""
    for section in sections:
        if re.match(r'^#{1,3}\s+', section):
            if current_text.strip():
                chunks.append({
                    "text": current_heading + current_text,
                    "metadata": {
                        "connector": connector,
                        "component_type": "documentation",
                        "auth_type": "",
                        "language": "markdown",
                        "file_name": file_name,
                        "chunk_part": "section",
                        "entity_type": "",
                    },
                })
            current_heading = section
            current_text = ""
        else:
            current_text += section

    if current_text.strip():
        chunks.append({
            "text": current_heading + current_text,
            "metadata": {
                "connector": connector,
                "component_type": "documentation",
                "auth_type": "",
                "language": "markdown",
                "file_name": file_name,
                "chunk_part": "section",
                "entity_type": "",
            },
        })

    return chunks


def _detect_entity_from_filename(file_name: str) -> str:
    """Guess entity type from file name."""
    name_lower = file_name.lower()
    if "content" in name_lower or "catalog" in name_lower or "course" in name_lower:
        return "content"
    if "user" in name_lower or "employee" in name_lower or "worker" in name_lower:
        return "user"
    if "transcript" in name_lower:
        return "transcript"
    if "learningpath" in name_lower or "curriculum" in name_lower or "pathway" in name_lower:
        return "learning_path"
    if "location" in name_lower:
        return "location"
    if "jobrole" in name_lower or "jobreq" in name_lower:
        return "job"
    if "skill" in name_lower or "competenc" in name_lower:
        return "skill"
    return ""


# ---------------------------------------------------------------------------
# Main chunking dispatcher
# ---------------------------------------------------------------------------


def chunk_file(
    content: str,
    file_name: str,
    connector: str,
) -> List[Dict[str, Any]]:
    """
    Dispatch to the appropriate chunking strategy based on file type.
    Returns list of {"text": str, "metadata": dict} chunks.
    """
    lang = _detect_language(file_name)

    if lang == "java":
        raw_chunks = chunk_java_file(content, file_name, connector)
    elif lang == "javascript":
        raw_chunks = chunk_js_flow_file(content, file_name, connector)
    elif lang == "xml":
        raw_chunks = chunk_xml_mapping_file(content, file_name, connector)
    elif lang == "sql":
        raw_chunks = chunk_sql_file(content, file_name, connector)
    elif lang == "markdown":
        raw_chunks = chunk_markdown_file(content, file_name, connector)
    else:
        # Fallback: whole file as one chunk
        raw_chunks = [{
            "text": content,
            "metadata": {
                "connector": connector,
                "component_type": _detect_component_type(file_name, content),
                "auth_type": _detect_auth_type(content),
                "language": lang,
                "file_name": file_name,
                "chunk_part": "full_file",
                "entity_type": "",
            },
        }]

    # Assign stable IDs
    for i, chunk in enumerate(raw_chunks):
        chunk["id"] = _generate_chunk_id(connector, file_name, i)

    logger.debug(f"  Chunked {file_name} → {len(raw_chunks)} chunks")
    return raw_chunks

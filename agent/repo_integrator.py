"""
agent/repo_integrator.py
Applies generated connector files to the actual SIH repository.
Maps generated files to correct repo paths and patches existing files.
"""

import os
import re
import shutil
import logging
from pathlib import Path
from typing import List, Dict

logger = logging.getLogger(__name__)

# Base paths relative to SIH_CODEBASE_PATH
JAVA_BASE = "integration/apps/src/main/java/com/saba/integration/apps"
RESOURCES_BASE = "integration/apps/src/main/resources/com/saba/mapping"
MARKETPLACE_BASE = "marketplace/src/main/java/com/saba/integration/marketplace"
DATABASE_BASE = "database"


def get_repo_file_map(connector_name: str) -> Dict[str, str]:
    """
    Map generated file types to their actual paths in the SIH repo.
    Returns: { file_type: relative_path_in_repo }
    """
    name_lower = connector_name.lower()
    name_cap = connector_name.capitalize()

    return {
        "constants": f"{JAVA_BASE}/{name_lower}/{name_cap}Constants.java",
        "test_connection": f"{JAVA_BASE}/{name_lower}/{name_cap}TestConnection.java",
        "component_control": f"{JAVA_BASE}/{name_lower}/{name_cap}ComponentControl.java",
        "flows": f"{JAVA_BASE}/{name_lower}/{name_cap}Flows.java",
        "flow_definition": f"{RESOURCES_BASE}/{name_lower}/flow/Content.js",
        "mapping_xml": f"{RESOURCES_BASE}/{name_lower}/default_data/{name_lower}_edcast_content.xml",
        "vendor_constants": f"{MARKETPLACE_BASE}/vendor/VendorConstants.java",
        "mapping_config": f"{MARKETPLACE_BASE}/mapping/util/DefaultMappingConfig.java",
        "dml": f"{DATABASE_BASE}/dml_patch_{name_lower}.sql",
    }


def preview_apply(
    connector_name: str,
    generated_files: List[Dict],
    sih_path: str,
) -> List[Dict]:
    """
    Preview what will happen when files are applied to the repo.
    Returns a list of actions (create, patch, overwrite) with details.
    """
    file_map = get_repo_file_map(connector_name)
    actions = []

    for gen_file in generated_files:
        file_type = gen_file["file_type"]
        repo_rel_path = file_map.get(file_type)
        if not repo_rel_path:
            continue

        full_path = os.path.join(sih_path, repo_rel_path)
        exists = os.path.exists(full_path)

        if file_type in ("vendor_constants", "mapping_config"):
            # These are PATCHES to existing files, not replacements
            action = {
                "file_type": file_type,
                "file_name": gen_file["file_name"],
                "repo_path": repo_rel_path,
                "full_path": full_path,
                "action": "PATCH" if exists else "CREATE",
                "exists": exists,
                "description": f"Insert new entry into existing {os.path.basename(repo_rel_path)}",
                "content": gen_file["content"],
                "is_patch": True,
            }
        elif file_type == "dml":
            action = {
                "file_type": file_type,
                "file_name": gen_file["file_name"],
                "repo_path": repo_rel_path,
                "full_path": full_path,
                "action": "CREATE",
                "exists": exists,
                "description": f"Create new DML script for {connector_name}",
                "content": gen_file["content"],
                "is_patch": False,
            }
        else:
            action = {
                "file_type": file_type,
                "file_name": gen_file["file_name"],
                "repo_path": repo_rel_path,
                "full_path": full_path,
                "action": "OVERWRITE" if exists else "CREATE",
                "exists": exists,
                "description": f"{'Overwrite' if exists else 'Create'} {os.path.basename(repo_rel_path)}",
                "content": gen_file["content"],
                "is_patch": False,
            }

        actions.append(action)

    return actions


def apply_to_repo(
    connector_name: str,
    generated_files: List[Dict],
    sih_path: str,
) -> Dict:
    """
    Apply generated files to the actual SIH repository.
    - New connector files: created in the correct package directory
    - VendorConstants/DefaultMappingConfig: patched in-place
    - DML: created as a new file
    Returns: { applied: [], skipped: [], errors: [] }
    """
    file_map = get_repo_file_map(connector_name)
    result = {"applied": [], "skipped": [], "errors": []}

    for gen_file in generated_files:
        file_type = gen_file["file_type"]
        repo_rel_path = file_map.get(file_type)
        if not repo_rel_path:
            result["skipped"].append({
                "file": gen_file["file_name"],
                "reason": f"No repo mapping for file_type '{file_type}'",
            })
            continue

        full_path = os.path.join(sih_path, repo_rel_path)

        try:
            if file_type == "vendor_constants" and os.path.exists(full_path):
                _patch_vendor_constants(full_path, gen_file["content"], connector_name)
                result["applied"].append({
                    "file": gen_file["file_name"],
                    "action": "PATCHED",
                    "path": repo_rel_path,
                })

            elif file_type == "mapping_config" and os.path.exists(full_path):
                _patch_mapping_config(full_path, gen_file["content"], connector_name)
                result["applied"].append({
                    "file": gen_file["file_name"],
                    "action": "PATCHED",
                    "path": repo_rel_path,
                })

            else:
                # Create new file (or overwrite)
                os.makedirs(os.path.dirname(full_path), exist_ok=True)
                with open(full_path, "w", encoding="utf-8") as f:
                    f.write(gen_file["content"])
                result["applied"].append({
                    "file": gen_file["file_name"],
                    "action": "CREATED",
                    "path": repo_rel_path,
                })

            logger.info(f"  ✅ Applied: {repo_rel_path}")

        except Exception as e:
            logger.error(f"  ❌ Failed: {repo_rel_path} — {e}")
            result["errors"].append({
                "file": gen_file["file_name"],
                "path": repo_rel_path,
                "error": str(e),
            })

    return result


def _patch_vendor_constants(file_path: str, patch_content: str, connector_name: str):
    """
    Insert new constant into VendorConstants.java.
    Finds the last 'public static final String' line and inserts after it.
    """
    with open(file_path, "r", encoding="utf-8") as f:
        original = f.read()

    # Check if already patched
    upper_name = connector_name.upper()
    if upper_name in original:
        logger.info(f"  VendorConstants already contains {upper_name}, skipping")
        return

    # Extract the constant line from the patch content
    const_lines = []
    for line in patch_content.split("\n"):
        line_stripped = line.strip()
        if line_stripped.startswith("public static final") and "=" in line_stripped:
            const_lines.append("    " + line_stripped)

    if not const_lines:
        raise ValueError("Could not extract constant from patch content")

    # Insert before the closing brace of the class
    insert_text = "\n" + "\n".join(const_lines) + "\n"
    # Find the last } in the file (class closing brace)
    last_brace = original.rfind("}")
    if last_brace == -1:
        raise ValueError("Could not find class closing brace")

    patched = original[:last_brace] + insert_text + original[last_brace:]

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(patched)


def _patch_mapping_config(file_path: str, patch_content: str, connector_name: str):
    """
    Insert new enum entry into DefaultMappingConfig.java.
    Finds the last enum entry and inserts the new one after it.
    """
    with open(file_path, "r", encoding="utf-8") as f:
        original = f.read()

    upper_name = connector_name.upper()
    if f"{upper_name}_CONTENT" in original:
        logger.info(f"  DefaultMappingConfig already contains {upper_name}_CONTENT, skipping")
        return

    # Extract the enum entry from patch content
    enum_lines = []
    capture = False
    for line in patch_content.split("\n"):
        if upper_name in line or capture:
            capture = True
            enum_lines.append("    " + line.strip())
            if line.strip().endswith("),") or line.strip().endswith(");"):
                break

    if not enum_lines:
        # Fallback: insert the entire patch as-is
        enum_lines = ["    // " + connector_name + " connector mapping"]
        for line in patch_content.split("\n"):
            if line.strip():
                enum_lines.append("    " + line.strip())

    # Find the last enum entry (line ending with ),) and insert after it
    lines = original.split("\n")
    insert_idx = -1
    for i in range(len(lines) - 1, -1, -1):
        if lines[i].strip().endswith("),"):
            insert_idx = i + 1
            break

    if insert_idx == -1:
        raise ValueError("Could not find enum insertion point")

    new_lines = lines[:insert_idx] + enum_lines + lines[insert_idx:]
    patched = "\n".join(new_lines)

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(patched)

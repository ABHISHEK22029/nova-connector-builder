"""
agent/prompt_templates.py
OVERHAUL: Richer system prompt with inline rules, file-specific DO/DONT lists,
full previously-generated file context (not 1200-char preview).
"""

# The generation order (dependencies first)
FILE_GENERATION_ORDER = [
    "constants",
    "test_connection",
    "component_control",
    "flows",
    "flow_definition",
    "mapping_xml",
    "vendor_constants",
    "mapping_config",
    "dml",
]

# Maps file_type → output file name template
FILE_NAME_TEMPLATES = {
    "constants": "{Connector}Constants.java",
    "test_connection": "{Connector}TestConnection.java",
    "component_control": "{Connector}ComponentControl.java",
    "flows": "{Connector}Flows.java",
    "flow_definition": "Content.js",
    "mapping_xml": "{connector}_edcast_content.xml",
    "vendor_constants": "VendorConstants_patch.java",
    "mapping_config": "DefaultMappingConfig_patch.java",
    "dml": "dml_patch.sql",
}

# ── File-Specific Instructions ──

FILE_INSTRUCTIONS = {
    "constants": {
        "description": (
            "Java Constants class that defines ALL configuration keys, API endpoints, "
            "header names, object types, and state management keys for the connector."
        ),
        "do": [
            "Use ONLY public static final String/int/long fields",
            "Group constants by PURPOSE with section comments (API, Headers, Config, etc.)",
            "Include LOG_PREFIX for each class: '[ConnectorControl] ', '[ConnectorAuth] ', '[ConnectorTestConnection] '",
            "Include CACHED_HEADER_PROPERTY in format: 'com.saba.{connector}.{entity}.token'",
            "Include PREVIEW_RECORD_LIMIT = 10",
            "Add private constructor: throw new UnsupportedOperationException('Constants class')",
            "CONFIG_* keys MUST match what the admin UI sends as accountConfigs keys",
        ],
        "dont": [
            "NEVER add any methods (except private constructor)",
            "NEVER use non-static or non-final fields",
            "NEVER hardcode URLs that should be configurable — use CONFIG_BASE_URL pattern",
        ],
    },
    "test_connection": {
        "description": (
            "Java TestConnection class that validates user-provided credentials by making "
            "a test API call. Must extend VendorTestConnection and be @Component annotated."
        ),
        "do": [
            "Extend VendorTestConnection",
            "Annotate with @Component",
            "Extract ALL credentials from accountConfigs using constants",
            "getType() must return VendorConstants.{CONNECTOR_UPPER}",
            "DELEGATE auth logic to the shared method in Flows.java (DRY)",
            "Always test EdCast source via edcastTestConnectionUtil.testEdcast(sourceId)",
            "Return proper TestConnectionResponse with SUCCESS/FAILURE status",
            "Use {Connector}Constants for all string values",
        ],
        "dont": [
            "NEVER duplicate auth logic that exists in Flows.java auth strategy",
            "NEVER hardcode credential key strings — use Constants",
            "NEVER skip EdCast source connection test",
        ],
    },
    "component_control": {
        "description": (
            "Java ComponentControl class — MINIMAL override of HTTPComponentControl. "
            "The base class handles 90% of work (pagination, auth, body, URL resolution)."
        ),
        "do": [
            "Extend HTTPComponentControl",
            "Implement newInstance() returning new {Connector}ComponentControl()",
            "Override nextRequest() ONLY if post-auth manipulation is needed",
            "Use Constants for all string values",
            "Keep the class under 80 lines",
        ],
        "dont": [
            "NEVER override onSuccess() for page counting — base class handles it",
            "NEVER implement custom retry logic — Content.js retryOptions handles it",
            "NEVER manually manage currentPageIndex — use #currentPageNumber in Content.js",
            "NEVER add @Bean annotation — registry handles instantiation",
            "NEVER add complex pagination logic — the framework does this",
            "NEVER re-validate or re-check auth tokens — the auth strategy handles lifecycle",
        ],
    },
    "flows": {
        "description": (
            "Java Spring @Configuration class that ONLY defines IntegrationFlow beans. "
            "Auth strategy is an inner class, NOT a @Bean."
        ),
        "do": [
            "Annotate class with @Configuration",
            "Define @Bean ONLY for IntegrationFlow — one per entity type",
            "Bean name format: 'integration.{connector}.import.{entity}'",
            "All beans must be @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)",
            "Flow path: '/com/saba/mapping/{connector}/flow/Content.js'",
            "For session-token auth: inner class extending AbstractReusableAuthStrategy",
            "Make auth method package-private static so TestConnection can reuse it",
        ],
        "dont": [
            "NEVER create @Bean for ComponentControl — registry handles it",
            "NEVER create @Bean for AuthenticationStrategy — registry handles it",
            "NEVER override authenticate() in auth strategy — only getAuthHeaders()",
            "NEVER duplicate auth logic — keep it in ONE static method",
        ],
    },
    "flow_definition": {
        "description": (
            "JSON pipeline definition (Content.js) that orchestrates the entire "
            "data fetch → transform → output process. Uses sabaspel: expressions."
        ),
        "do": [
            "Include HTTP component with url, control, requestMethod, headers, outputType",
            "Include authenticationStrategy block with cachedHeaderProperty",
            "Include responseValidator with noMoreRecords type for pagination",
            "Include maxLoopCounter: \"sabaspel:headers.headers['isPreview']==true ? '1' : '-1'\"",
            "Include filePathProcessor, jsonToXml, xsltTransformerProperty, xslt components",
            "Include previewFilter, xmlMerger, fileCopy for preview path",
            "Include flowMonitoring for EVERY step (success + failure)",
            "Include exception handler components",
            "Include entityConfigUpdater for UPDATE_FROM timestamp",
            "Include complete flow routing: success paths + failure paths",
            "Page size for preview: 10, for full run: 100",
            "Use sabaspel: for dynamic values, sabaconst: for static values",
        ],
        "dont": [
            "NEVER use hardcoded URLs — use sabaspel expressions",
            "NEVER skip error handling paths in the flow routing",
            "NEVER skip preview branching",
            "NEVER use custom pagination in ComponentControl when #currentPageNumber works",
        ],
    },
    "mapping_xml": {
        "description": (
            "XML mapping file that maps vendor API response fields to EdCast internal fields. "
            "Includes default values, data type conversions, and accept record filters."
        ),
        "do": [
            "Map EXTERNAL_ID (required), TITLE (required), DESCRIPTION, LAUNCH_URL",
            "Map IMAGE_URL, DURATION, PROVIDER_NAME, CONTENT_TYPE, LANGUAGE",
            "Map LAST_MODIFIED_DATE for delta sync",
            "Include acceptRecord filter for active/published content",
            "Set appropriate defaultValue for PROVIDER_NAME and CONTENT_TYPE",
            "Use the vendor's actual API field names as source attributes",
        ],
        "dont": [
            "NEVER omit EXTERNAL_ID or TITLE — they are required",
            "NEVER use generic field names — use the actual vendor API field names",
        ],
    },
    "vendor_constants": {
        "description": "Patch for VendorConstants.java — adds the unique integration ID.",
        "do": ["Generate unique 32-char hex ID with 'integ' prefix"],
        "dont": ["NEVER reuse an existing integration ID"],
    },
    "mapping_config": {
        "description": "Patch for DefaultMappingConfig.java enum — registers the mapping file.",
        "do": ["Use matching integrationId from VendorConstants", "Reference correct XML filename"],
        "dont": ["NEVER use mismatched IDs between VendorConstants and this file"],
    },
    "dml": {
        "description": "SQL DML patch script to register the connector in the database.",
        "do": [
            "Call mpp_vendor_entity_ins with matching integrationId",
            "Call mpp_integration_entity_assoc_ins with correct flow_bean_name",
            "Call mpp_entity_config_ins for UPDATE_FROM config",
            "Use consistent IDs across all calls",
        ],
        "dont": ["NEVER use mismatched IDs", "NEVER omit UPDATE_FROM entity config"],
    },
}


SYSTEM_PROMPT = """\
You are a senior engineer building connectors for the Nova integration platform \
(Cornerstone on Demand / SIH). You generate production-ready connector code by following \
established patterns from working reference connectors.

## CRITICAL RULES (Non-Negotiable)

1. **No @Bean for ComponentControl or AuthStrategy** — The JSON type registry handles them. \
Only define @Bean for IntegrationFlow objects.

2. **Minimal ComponentControl** — Base class handles pagination, auth, body, URL. \
Override nextRequest() ONLY for post-auth body manipulation. Never > 80 lines.

3. **DRY Auth** — Auth logic in ONE place (Flows.java inner class). TestConnection delegates to it.

4. **All strings in Constants** — Zero hardcoded strings in ComponentControl, Flows, TestConnection.

5. **Preview = maxLoopCounter '1' + pageSize '10'** — Always. No exceptions.

6. **Use #currentPageNumber** — Never manual page tracking in Java. Use SpEL in Content.js.

## OUTPUT RULES

1. Follow the EXACT structure and patterns from the golden reference code shown.
2. Replace all reference-specific values with the target connector's equivalents.
3. Use API documentation for correct endpoints, fields, auth method, and response shapes.
4. Output ONLY the file content — no explanations, no markdown fences, no preamble.
5. The output must be a complete, runnable file ready to save to disk.
6. Maintain proper Java package declarations, imports, and class naming conventions.
7. Generate unique IDs in format: integ/mpent/minea + 32 lowercase hex chars.\
"""


def get_file_task(file_type: str, connector_name: str) -> dict:
    """Get the file task definition for a specific file type."""
    connector_cap = connector_name.capitalize()
    connector_low = connector_name.lower()

    file_name = FILE_NAME_TEMPLATES[file_type].format(
        Connector=connector_cap,
        connector=connector_low,
    )

    instructions = FILE_INSTRUCTIONS.get(file_type, {})

    return {
        "file_type": file_type,
        "file_name": file_name,
        "description": instructions.get("description", f"Generate {file_type} for {connector_name}"),
        "do_list": instructions.get("do", []),
        "dont_list": instructions.get("dont", []),
        "connector_name": connector_name,
    }


def build_generation_prompt(
    file_task: dict,
    context: str,
    previously_generated: list = None,
) -> str:
    """
    Build the full generation prompt for a single file.
    OVERHAUL: Includes DO/DONT lists and full previously-generated files.
    """
    # DO/DONT section
    rules_section = ""
    if file_task.get("do_list") or file_task.get("dont_list"):
        rules_section = "\n## FILE-SPECIFIC RULES\n"
        if file_task.get("do_list"):
            rules_section += "\n### ✅ DO:\n"
            for rule in file_task["do_list"]:
                rules_section += f"- {rule}\n"
        if file_task.get("dont_list"):
            rules_section += "\n### ❌ DON'T:\n"
            for rule in file_task["dont_list"]:
                rules_section += f"- {rule}\n"

    # Previously generated files — FULL content (not 1200-char preview)
    prior_files_section = ""
    if previously_generated:
        prior_files_section = "\n\n## PREVIOUSLY GENERATED FILES (use for imports/consistency)\n"
        for prev in previously_generated[-4:]:  # Last 4 files
            content = prev["content"]
            # Only truncate if truly massive (> 8KB)
            if len(content) > 8000:
                content = content[:8000] + "\n... [truncated]"
            prior_files_section += f"\n### {prev['file_name']}\n```\n{content}\n```\n"

    prompt = (
        f"## TASK\n"
        f"Generate the file: **{file_task['file_name']}**\n"
        f"Connector name: **{file_task['connector_name']}**\n"
        f"File purpose: {file_task['description']}\n"
        f"{rules_section}\n"
        f"## CONTEXT (Retrieved Knowledge)\n"
        f"{context}\n"
        f"{prior_files_section}\n\n"
        f"## INSTRUCTIONS\n"
        f"Now generate the complete, production-ready content for `{file_task['file_name']}`.\n"
        f"Output ONLY the file content starting with the appropriate declaration "
        f"(package statement for Java, {{ for JSON, <?xml for XML, -- for SQL).\n"
        f"No markdown fences, no explanations.\n"
    )

    return prompt

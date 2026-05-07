"""
agent/validator.py
OVERHAUL: Production-readiness validation checks for generated connector code.
Catches anti-patterns, missing conventions, and cross-file inconsistencies.
"""

import re
import logging
from typing import List, Dict, Tuple, Any
from dataclasses import dataclass

logger = logging.getLogger(__name__)


@dataclass
class ValidationResult:
    file_name: str
    file_type: str
    passed: bool
    errors: List[str]
    warnings: List[str]

    def __str__(self):
        status = "✅ PASS" if self.passed else "❌ FAIL"
        msg = f"{status} {self.file_name}"
        if self.errors:
            msg += f" ({len(self.errors)} errors)"
        if self.warnings:
            msg += f" ({len(self.warnings)} warnings)"
        return msg


# ── Generic Validators ──

def _check_no_raw_strings(content: str, file_name: str) -> List[str]:
    """Check for hardcoded strings that should be constants."""
    errors = []
    # Skip constants file itself
    if "Constants" in file_name:
        return errors

    # Common hardcoded patterns
    patterns = [
        (r'"isPreview"', "Use {Connector}Constants.HEADER_IS_PREVIEW instead of hardcoded 'isPreview'"),
        (r'"INTEGRATION_MONITORING_ID"', "Use Constants.HEADER_INTEGRATION_MONITORING_ID"),
        (r'"Content-Type"', "Acceptable in headers map, but consider using Constants"),
    ]
    for pattern, msg in patterns:
        if re.search(pattern, content) and "Constants" not in file_name:
            errors.append(f"Possible hardcoded string: {msg}")
    return errors


def _check_not_empty(content: str) -> List[str]:
    """Check file is not empty or stub."""
    errors = []
    if len(content.strip()) < 50:
        errors.append("File appears empty or is just a stub")
    if "GENERATION FAILED" in content or "MOCK GENERATED" in content:
        errors.append("File contains generation failure marker")
    return errors


# ── File-Specific Validators ──

def _validate_constants(content: str, file_name: str) -> Tuple[List[str], List[str]]:
    errors = []
    warnings = []

    if "public class" not in content:
        errors.append("Missing class declaration")
    if "private " in content and "Constants()" not in content:
        warnings.append("Constants class should only have private constructor")
    if "void " in content or "return " in content:
        errors.append("Constants class should have NO methods (except private constructor)")
    if "LOG_PREFIX" not in content:
        warnings.append("Missing LOG_PREFIX constant — add for each class that logs")
    if "PREVIEW_RECORD_LIMIT" not in content and "PREVIEW" not in content:
        warnings.append("Missing PREVIEW_RECORD_LIMIT — should be 10")
    if "private " not in content or "UnsupportedOperationException" not in content:
        warnings.append("Missing private constructor with UnsupportedOperationException")

    return errors, warnings


def _validate_test_connection(content: str, file_name: str) -> Tuple[List[str], List[str]]:
    errors = []
    warnings = []

    if "@Component" not in content:
        errors.append("Missing @Component annotation")
    if "VendorTestConnection" not in content and "extends" not in content:
        errors.append("Must extend VendorTestConnection")
    if "getType()" not in content:
        errors.append("Missing getType() method")
    if "VendorConstants." not in content:
        errors.append("getType() should return VendorConstants.{CONNECTOR}")
    if "testConnection(" not in content:
        errors.append("Missing testConnection() method override")
    if "edcastTestConnectionUtil" not in content and "edcast" not in content.lower():
        warnings.append("Missing EdCast source connection test")

    # DRY check: should delegate auth, not duplicate it
    if content.count("RestTemplate") > 1:
        warnings.append("Multiple RestTemplate usages — consider delegating to Flows auth method")

    return errors, warnings


def _validate_component_control(content: str, file_name: str) -> Tuple[List[str], List[str]]:
    errors = []
    warnings = []

    if "HTTPComponentControl" not in content:
        errors.append("Must extend HTTPComponentControl")
    if "newInstance()" not in content:
        errors.append("Missing newInstance() method")

    # Anti-pattern checks
    if "currentPageIndex" in content or "pageNumber" in content:
        errors.append("ANTI-PATTERN: Manual page tracking. Use #currentPageNumber in Content.js")
    if "onSuccess" in content and "page" in content.lower():
        errors.append("ANTI-PATTERN: Overriding onSuccess() for page counting")
    if "@Bean" in content:
        errors.append("ANTI-PATTERN: @Bean annotation. Registry handles instantiation")

    # Size check
    lines = content.strip().split('\n')
    if len(lines) > 120:
        warnings.append(f"ComponentControl is {len(lines)} lines — should be under 80. Keep it minimal.")
    elif len(lines) > 80:
        warnings.append(f"ComponentControl is {len(lines)} lines — consider simplifying")

    return errors, warnings


def _validate_flows(content: str, file_name: str) -> Tuple[List[str], List[str]]:
    errors = []
    warnings = []

    if "@Configuration" not in content:
        errors.append("Missing @Configuration annotation")
    if "@Bean" not in content:
        errors.append("Missing @Bean for IntegrationFlow")
    if "SCOPE_PROTOTYPE" not in content:
        errors.append("Missing SCOPE_PROTOTYPE — all beans must be prototype-scoped")

    # Anti-pattern: @Bean for ComponentControl or AuthStrategy
    bean_pattern = re.findall(r'@Bean[^}]*?public\s+(\w+)\s+', content)
    for bean_type in bean_pattern:
        if "ComponentControl" in bean_type:
            errors.append(f"ANTI-PATTERN: @Bean for {bean_type}. Registry handles ComponentControl.")
        if "AuthenticationStrategy" in bean_type or "AuthStrategy" in bean_type:
            errors.append(f"ANTI-PATTERN: @Bean for {bean_type}. Registry handles auth strategies.")

    if "FlowGraphCreator.generateIntegrationFlow" not in content:
        errors.append("Missing FlowGraphCreator.generateIntegrationFlow() call")

    return errors, warnings


def _validate_flow_definition(content: str, file_name: str) -> Tuple[List[str], List[str]]:
    errors = []
    warnings = []

    if '"components"' not in content:
        errors.append("Missing 'components' array")
    if '"flow"' not in content:
        errors.append("Missing 'flow' routing section")
    if '"success"' not in content:
        errors.append("Missing 'success' flow routes")
    if '"failure"' not in content:
        errors.append("Missing 'failure' flow routes")

    # Required components
    required = ['"http"', '"jsonToXml"', '"xslt"', '"flowMonitoring"', '"exception"', '"router"']
    for req in required:
        if req not in content:
            errors.append(f"Missing required component type: {req}")

    # Preview checks
    if "maxLoopCounter" not in content:
        errors.append("Missing maxLoopCounter for preview control")
    if "isPreview" not in content:
        warnings.append("Missing isPreview checks for preview/full-run branching")
    if "previewFilter" not in content:
        warnings.append("Missing previewFilter component for preview branching")

    # Auth
    if "authenticationStrategy" not in content:
        errors.append("Missing authenticationStrategy block")
    if "cachedHeaderProperty" not in content:
        warnings.append("Missing cachedHeaderProperty — auth tokens won't be cached")

    # Monitoring
    if "flowMonitoring" not in content:
        errors.append("Missing flowMonitoring components")

    # Entity config updater
    if "entityConfigUpdater" not in content:
        warnings.append("Missing entityConfigUpdater — UPDATE_FROM won't be saved")

    return errors, warnings


def _validate_mapping_xml(content: str, file_name: str) -> Tuple[List[str], List[str]]:
    errors = []
    warnings = []

    if "EXTERNAL_ID" not in content:
        errors.append("Missing EXTERNAL_ID mapping (required)")
    if "TITLE" not in content:
        errors.append("Missing TITLE mapping (required)")
    if "LAUNCH_URL" not in content:
        warnings.append("Missing LAUNCH_URL mapping")
    if "DESCRIPTION" not in content:
        warnings.append("Missing DESCRIPTION mapping")

    return errors, warnings


def _validate_dml(content: str, file_name: str) -> Tuple[List[str], List[str]]:
    errors = []
    warnings = []

    if "mpp_vendor_entity_ins" not in content:
        errors.append("Missing mpp_vendor_entity_ins call")
    if "mpp_integration_entity_assoc_ins" not in content:
        errors.append("Missing mpp_integration_entity_assoc_ins call")
    if "UPDATE_FROM" not in content:
        warnings.append("Missing UPDATE_FROM entity config registration")

    return errors, warnings


# ── Dispatcher ──

VALIDATORS = {
    "constants": _validate_constants,
    "test_connection": _validate_test_connection,
    "component_control": _validate_component_control,
    "flows": _validate_flows,
    "flow_definition": _validate_flow_definition,
    "mapping_xml": _validate_mapping_xml,
    "vendor_constants": lambda c, f: ([], []),
    "mapping_config": lambda c, f: ([], []),
    "dml": _validate_dml,
}


def validate_file(content: str, file_name: str, file_type: str) -> ValidationResult:
    """Validate a single generated file."""
    errors = _check_not_empty(content)
    warnings = []

    # File-specific validation
    validator = VALIDATORS.get(file_type)
    if validator:
        file_errors, file_warnings = validator(content, file_name)
        errors.extend(file_errors)
        warnings.extend(file_warnings)

    # Generic checks (skip for non-Java)
    if file_name.endswith('.java'):
        errors.extend(_check_no_raw_strings(content, file_name))

    result = ValidationResult(
        file_name=file_name,
        file_type=file_type,
        passed=len(errors) == 0,
        errors=errors,
        warnings=warnings,
    )

    if errors:
        logger.warning(f"Validation FAILED for {file_name}: {errors}")
    if warnings:
        logger.info(f"Validation warnings for {file_name}: {warnings}")

    return result


def validate_all(generated_files: List[Dict]) -> Tuple[bool, List[ValidationResult]]:
    """Validate all generated files and check cross-file consistency."""
    results = []
    all_passed = True

    for f in generated_files:
        result = validate_file(f["content"], f["file_name"], f["file_type"])
        results.append(result)
        if not result.passed:
            all_passed = False

    # Cross-file consistency checks
    cross_errors = _cross_file_validation(generated_files)
    if cross_errors:
        all_passed = False
        cross_result = ValidationResult(
            file_name="[Cross-File]",
            file_type="cross_validation",
            passed=False,
            errors=cross_errors,
            warnings=[],
        )
        results.append(cross_result)

    return all_passed, results


def _cross_file_validation(generated_files: List[Dict]) -> List[str]:
    """Check consistency across generated files."""
    errors = []
    files_by_type = {f["file_type"]: f["content"] for f in generated_files}

    # Check: Constants referenced in other files exist
    constants_content = files_by_type.get("constants", "")
    if constants_content:
        # Extract constant names
        constant_names = set(re.findall(r'public static final \w+ (\w+)\s*=', constants_content))

        for file_type, content in files_by_type.items():
            if file_type == "constants":
                continue
            if not content or not content.strip():
                continue
            # Find references to Constants class
            refs = re.findall(r'\w+Constants\.(\w+)', content)
            for ref in refs:
                if ref not in constant_names and ref not in ("class", "java"):
                    errors.append(
                        f"[{file_type}] References constant '{ref}' not found in Constants file"
                    )

    return errors

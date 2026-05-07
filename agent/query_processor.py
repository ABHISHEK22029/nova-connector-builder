"""
agent/query_processor.py
Parses user prompts to extract connector requirements.
"""

import re
import logging
from typing import Dict, List, Optional
from dataclasses import dataclass, field

logger = logging.getLogger(__name__)


@dataclass
class ConnectorSpec:
    """Parsed specification from a user prompt."""
    connector_name: str
    auth_type: str = "unknown"
    entity_types: List[str] = field(default_factory=lambda: ["content"])
    sync_direction: str = "IMPORT"
    base_url: str = ""
    description: str = ""
    raw_prompt: str = ""

    def to_search_queries(self) -> List[str]:
        """Generate multiple search queries for better retrieval."""
        queries = [
            f"{self.connector_name} connector authentication pattern {self.auth_type}",
            f"{self.connector_name} API integration constants and configuration",
            f"connector flow definition for {self.entity_types[0]} sync",
            f"XML mapping template for {self.entity_types[0]} entity",
            f"test connection implementation for {self.auth_type} authentication",
            f"component control pagination and request handling",
            f"DML SQL registration for new connector integration",
        ]
        return queries


# Auth type keywords to look for in user prompts
AUTH_KEYWORDS = {
    "oauth2": ["oauth", "oauth2", "access_token", "client_credentials"],
    "basic_auth": ["basic auth", "basic authentication", "username password"],
    "api_key": ["api key", "api_key", "apikey"],
    "session_token": ["session", "session token"],
    "bearer_token": ["bearer", "bearer token"],
}

# Entity type keywords
ENTITY_KEYWORDS = {
    "content": ["content", "course", "catalog", "video", "learning"],
    "user": ["user", "employee", "worker", "person"],
    "transcript": ["transcript", "completion", "enrollment"],
    "learning_path": ["learning path", "curriculum", "pathway", "playlist"],
    "location": ["location", "site"],
    "job": ["job", "role", "position"],
}


def parse_prompt(prompt: str) -> ConnectorSpec:
    """
    Parse a user prompt into a structured ConnectorSpec.
    
    Examples:
        "Create a Stripe connector with OAuth2 for content sync"
        "Build HubSpot integration using API key"
        "Generate Zoom connector"
    """
    prompt_lower = prompt.lower()

    # Extract connector name - look for known patterns
    connector_name = _extract_connector_name(prompt)

    # Detect auth type
    auth_type = "unknown"
    for auth, keywords in AUTH_KEYWORDS.items():
        if any(kw in prompt_lower for kw in keywords):
            auth_type = auth
            break

    # Detect entity types
    entity_types = []
    for entity, keywords in ENTITY_KEYWORDS.items():
        if any(kw in prompt_lower for kw in keywords):
            entity_types.append(entity)
    if not entity_types:
        entity_types = ["content"]  # default

    # Detect sync direction
    sync_direction = "IMPORT"
    if any(kw in prompt_lower for kw in ["export", "push", "send"]):
        sync_direction = "EXPORT"

    spec = ConnectorSpec(
        connector_name=connector_name,
        auth_type=auth_type,
        entity_types=entity_types,
        sync_direction=sync_direction,
        raw_prompt=prompt,
    )

    logger.info(f"Parsed spec: {spec}")
    return spec


def _extract_connector_name(prompt: str) -> str:
    """Extract the connector/vendor name from the prompt."""
    # Common patterns: "Create a {name} connector", "Build {name} integration"
    patterns = [
        r"(?:create|build|generate|make)\s+(?:a\s+)?(\w+)\s+(?:connector|integration)",
        r"(\w+)\s+connector",
        r"(\w+)\s+integration",
        r"connector\s+for\s+(\w+)",
        r"integrate\s+(?:with\s+)?(\w+)",
    ]

    for pattern in patterns:
        match = re.search(pattern, prompt, re.IGNORECASE)
        if match:
            name = match.group(1).strip()
            # Filter out common non-name words
            if name.lower() not in {"a", "an", "the", "new", "this", "my", "our"}:
                return name.capitalize()

    # Fallback: first capitalized word that's not a common verb
    words = prompt.split()
    skip_words = {"create", "build", "generate", "make", "a", "an", "the", "with", "for", "using"}
    for word in words:
        clean = word.strip(".,!?")
        if clean.lower() not in skip_words and clean[0].isupper():
            return clean

    return "NewConnector"

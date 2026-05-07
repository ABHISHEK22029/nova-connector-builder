"""
agent/generator.py
LLM code generation — supports Gemini, OpenRouter, Groq, Cerebras, OpenAI, Anthropic, and CSOD gateway.
Priority: CSOD Gateway → Gemini → OpenRouter → Groq → Cerebras → OpenAI → Anthropic → Mock
"""

import os
import sys
import time
import logging
import re
import requests

from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger(__name__)

# Gateway config
GATEWAY_URL = os.getenv("LLM_GATEWAY_URL", "")
GATEWAY_TOKEN = os.getenv("LLM_GATEWAY_TOKEN", "").strip()
GATEWAY_MODEL = os.getenv("LLM_GATEWAY_MODEL", "anthropic-sonnet-4.0")

# API keys
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY", "").strip()
OPENROUTER_MODEL = os.getenv("OPENROUTER_MODEL", "google/gemini-2.0-flash-001")
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "").strip()
GEMINI_MODEL = os.getenv("GEMINI_MODEL", "gemini-2.0-flash")
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "").strip()
OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-4o")
ANTHROPIC_API_KEY = os.getenv("ANTHROPIC_API_KEY", "").strip()
GROQ_API_KEY = os.getenv("GROQ_API_KEY", "").strip()
GROQ_MODEL = os.getenv("GROQ_MODEL", "llama-3.3-70b-versatile")
CEREBRAS_API_KEY = os.getenv("CEREBRAS_API_KEY", "").strip()
CEREBRAS_MODEL = os.getenv("CEREBRAS_MODEL", "llama-3.3-70b")


def _has_key(val: str) -> bool:
    """Check if a key is set and not a placeholder."""
    return bool(val) and val not in ("your_bearer_token_here", "")


class ConnectorGenerator:
    """
    Generates connector code using an LLM.
    Priority: CSOD Gateway → Gemini → OpenRouter → Groq → Cerebras → OpenAI → Anthropic → Mock
    """

    def __init__(self):
        self.backend = "mock"

        if _has_key(GATEWAY_TOKEN):
            self.backend = "gateway"
        elif _has_key(GEMINI_API_KEY):
            self.backend = "gemini"
        elif _has_key(OPENROUTER_API_KEY):
            self.backend = "openrouter"
        elif _has_key(GROQ_API_KEY):
            self.backend = "groq"
        elif _has_key(CEREBRAS_API_KEY):
            self.backend = "cerebras"
        elif _has_key(OPENAI_API_KEY):
            self.backend = "openai"
        elif _has_key(ANTHROPIC_API_KEY):
            self.backend = "anthropic"

        backend_labels = {
            "gateway": f"CSOD Gateway ({GATEWAY_MODEL})",
            "gemini": f"Google Gemini ({GEMINI_MODEL})",
            "openrouter": f"OpenRouter ({OPENROUTER_MODEL})",
            "groq": f"Groq ({GROQ_MODEL})",
            "cerebras": f"Cerebras ({CEREBRAS_MODEL})",
            "openai": f"OpenAI ({OPENAI_MODEL})",
            "anthropic": "Anthropic Claude",
            "mock": "⚠️  MOCK MODE (no API key configured)",
        }

        logger.info(f"✅ LLM Backend: {backend_labels[self.backend]}")

    def generate(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int = 8192,
        temperature: float = 0.1,
    ) -> str:
        """Generate code via the configured LLM backend."""
        dispatch = {
            "gateway": self._generate_gateway,
            "gemini": self._generate_gemini,
            "openrouter": self._generate_openrouter,
            "groq": self._generate_groq,
            "cerebras": self._generate_cerebras,
            "openai": self._generate_openai,
            "anthropic": self._generate_anthropic,
        }

        fn = dispatch.get(self.backend)
        if fn:
            return fn(system_prompt, user_prompt, max_tokens, temperature)
        return self._generate_mock(system_prompt, user_prompt)

    # ── OpenRouter (via OpenAI-compatible API) ──

    def _generate_openrouter(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int,
        temperature: float,
    ) -> str:
        """Generate via OpenRouter (OpenAI-compatible endpoint)."""
        from openai import OpenAI

        client = OpenAI(
            base_url="https://openrouter.ai/api/v1",
            api_key=OPENROUTER_API_KEY,
        )

        logger.info(f"Calling OpenRouter ({OPENROUTER_MODEL})...")

        response = client.chat.completions.create(
            model=OPENROUTER_MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            max_tokens=max_tokens,
            temperature=temperature,
        )

        content = response.choices[0].message.content
        if not content:
            raise RuntimeError("Empty response from OpenRouter")

        usage = response.usage
        if usage:
            logger.info(
                f"OpenRouter: {usage.prompt_tokens} prompt + "
                f"{usage.completion_tokens} completion tokens"
            )

        return self._clean_output(content)

    # ── Google Gemini ──

    def _generate_gemini(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int,
        temperature: float,
    ) -> str:
        """Generate via Google Gemini API with auto-retry on rate limits."""
        from google import genai
        from google.genai import types

        client = genai.Client(api_key=GEMINI_API_KEY)

        max_retries = 3
        for attempt in range(max_retries):
            try:
                logger.info(f"Calling Gemini ({GEMINI_MODEL})... (attempt {attempt + 1})")

                response = client.models.generate_content(
                    model=GEMINI_MODEL,
                    contents=user_prompt,
                    config=types.GenerateContentConfig(
                        system_instruction=system_prompt,
                        max_output_tokens=max_tokens,
                        temperature=temperature,
                    ),
                )

                content = response.text
                if not content:
                    raise RuntimeError("Empty response from Gemini API")

                logger.info(f"Gemini response received ({len(content)} chars)")
                return self._clean_output(content)

            except Exception as e:
                error_str = str(e)
                if "429" in error_str or "RESOURCE_EXHAUSTED" in error_str:
                    wait_time = 60
                    match = re.search(r'retry in (\d+)', error_str.lower())
                    if match:
                        wait_time = int(match.group(1)) + 5

                    if attempt < max_retries - 1:
                        logger.warning(f"Rate limited. Waiting {wait_time}s before retry...")
                        time.sleep(wait_time)
                    else:
                        raise RuntimeError(
                            f"Gemini rate limit exceeded after {max_retries} retries. "
                            f"Try again later or use OpenRouter."
                        )
                else:
                    raise

    # ── OpenAI ──

    def _generate_openai(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int,
        temperature: float,
    ) -> str:
        """Generate via OpenAI API."""
        from openai import OpenAI

        client = OpenAI(api_key=OPENAI_API_KEY)
        logger.info(f"Calling OpenAI ({OPENAI_MODEL})...")

        response = client.chat.completions.create(
            model=OPENAI_MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            max_tokens=max_tokens,
            temperature=temperature,
        )

        content = response.choices[0].message.content
        if not content:
            raise RuntimeError("Empty response from OpenAI API")

        return self._clean_output(content)

    # ── CSOD Gateway ──

    def _generate_gateway(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int,
        temperature: float,
    ) -> str:
        """Generate via CSOD internal LLM gateway."""
        payload = {
            "model": GATEWAY_MODEL,
            "system_message": system_prompt,
            "prompt": user_prompt,
            "max_tokens": max_tokens,
            "temperature": temperature,
            "stream": False,
            "enable_guardrails": False,
        }

        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {GATEWAY_TOKEN}",
        }

        logger.info(f"Calling LLM gateway ({GATEWAY_MODEL})...")
        response = requests.post(GATEWAY_URL, json=payload, headers=headers, timeout=180)

        if response.status_code != 200:
            raise RuntimeError(f"Gateway error {response.status_code}: {response.text[:500]}")

        data = response.json()
        content = self._extract_content(data)
        if not content:
            raise RuntimeError(f"Could not extract text. Keys: {list(data.keys())}")

        return self._clean_output(content)

    # ── Anthropic ──

    def _generate_anthropic(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int,
        temperature: float,
    ) -> str:
        """Generate via Anthropic API."""
        payload = {
            "model": "claude-sonnet-4-20250514",
            "max_tokens": max_tokens,
            "temperature": temperature,
            "system": system_prompt,
            "messages": [{"role": "user", "content": user_prompt}],
        }

        headers = {
            "Content-Type": "application/json",
            "x-api-key": ANTHROPIC_API_KEY,
            "anthropic-version": "2023-06-01",
        }

        logger.info("Calling Anthropic API...")
        response = requests.post(
            "https://api.anthropic.com/v1/messages", json=payload, headers=headers, timeout=180
        )

        if response.status_code != 200:
            raise RuntimeError(f"Anthropic error {response.status_code}: {response.text[:500]}")

        data = response.json()
        content = data.get("content", [{}])[0].get("text", "")
        if not content:
            raise RuntimeError("Empty response from Anthropic API")

        return self._clean_output(content)

    # ── Groq ──

    def _generate_groq(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int,
        temperature: float,
    ) -> str:
        """Generate via Groq API (OpenAI-compatible)."""
        from openai import OpenAI

        client = OpenAI(
            base_url="https://api.groq.com/openai/v1",
            api_key=GROQ_API_KEY,
        )

        logger.info(f"Calling Groq ({GROQ_MODEL})...")

        response = client.chat.completions.create(
            model=GROQ_MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            max_tokens=max_tokens,
            temperature=temperature,
        )

        content = response.choices[0].message.content
        if not content:
            raise RuntimeError("Empty response from Groq")

        usage = response.usage
        if usage:
            logger.info(
                f"Groq: {usage.prompt_tokens} prompt + "
                f"{usage.completion_tokens} completion tokens"
            )

        return self._clean_output(content)

    # ── Cerebras ──

    def _generate_cerebras(
        self,
        system_prompt: str,
        user_prompt: str,
        max_tokens: int,
        temperature: float,
    ) -> str:
        """Generate via Cerebras API (OpenAI-compatible)."""
        from openai import OpenAI

        client = OpenAI(
            base_url="https://api.cerebras.ai/v1",
            api_key=CEREBRAS_API_KEY,
        )

        logger.info(f"Calling Cerebras ({CEREBRAS_MODEL})...")

        response = client.chat.completions.create(
            model=CEREBRAS_MODEL,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
            max_tokens=max_tokens,
            temperature=temperature,
        )

        content = response.choices[0].message.content
        if not content:
            raise RuntimeError("Empty response from Cerebras")

        return self._clean_output(content)

    # ── Mock ──

    def _generate_mock(self, system_prompt: str, user_prompt: str) -> str:
        """Mock generation when no API key is configured."""
        logger.warning("MOCK MODE — no LLM credentials configured")
        return (
            f"// MOCK GENERATED FILE\n"
            f"// Configure GEMINI_API_KEY, GROQ_API_KEY, or OPENROUTER_API_KEY in .env\n"
        )

    # ── Helpers ──

    def _extract_content(self, data: dict) -> str:
        """Extract text from various LLM gateway response formats."""
        return (
            data.get("text")
            or data.get("content")
            or data.get("output")
            or data.get("completion")
            or data.get("message", {}).get("content")
            or data.get("choices", [{}])[0].get("message", {}).get("content")
            or data.get("choices", [{}])[0].get("text")
            or ""
        )

    def _clean_output(self, content: str) -> str:
        """Strip markdown fences and whitespace from LLM output."""
        content = content.strip()
        if content.startswith("```"):
            lines = content.split("\n")
            start = 1
            end = len(lines) - 1 if lines[-1].strip() == "```" else len(lines)
            content = "\n".join(lines[start:end])
        return content.strip()

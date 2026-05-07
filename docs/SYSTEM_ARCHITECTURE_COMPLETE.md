# Nova Connector Builder — Complete System Architecture Document

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [System Architecture Diagram](#3-system-architecture-diagram)
4. [Module-by-Module Breakdown](#4-module-by-module-breakdown)
5. [Data Flow — End to End](#5-data-flow--end-to-end)
6. [AI/ML Models Used](#6-aiml-models-used)
7. [Frontend ↔ Backend Linkage](#7-frontend--backend-linkage)
8. [API Endpoints Reference](#8-api-endpoints-reference)
9. [Vector Database Design](#9-vector-database-design)
10. [RAG Pipeline Deep Dive](#10-rag-pipeline-deep-dive)
11. [File Structure](#11-file-structure)
12. [Environment Configuration](#12-environment-configuration)
13. [How to Run](#13-how-to-run)

---

## 1. Project Overview

**Nova Connector Builder** is an **Agentic RAG (Retrieval-Augmented Generation)** system that automates the generation of production-ready integration connectors for the Cornerstone/EdCast (Nova) platform.

### What It Does
- Accepts a natural language prompt (e.g., *"Create a Zoom connector with OAuth2"*)
- Retrieves relevant code patterns from existing connectors (Kaltura, LinkedIn Learning, Udemy, etc.)
- Generates **9 production-ready files** (Java, JSON, XML, SQL) using an LLM
- Validates the output for syntax correctness
- Stores the generated output for future retrieval (feedback loop)

### Why RAG?
Traditional code generation (zero-shot LLM) produces generic boilerplate. By feeding the LLM **actual working connector code** as context, it generates output that follows the exact patterns, naming conventions, ID formats, and framework APIs of the Nova platform. This is the core advantage of Retrieval-Augmented Generation over plain prompting.

---

## 2. Technology Stack

### 2.1 Frontend
| Technology | Role | Details |
|---|---|---|
| **HTML5** | Page structure | Single-page app with semantic markup |
| **Vanilla CSS** | Styling | Dark theme, glassmorphism, CSS custom properties |
| **Vanilla JavaScript** | Logic | Fetch API for HTTP calls, DOM manipulation |
| **Google Fonts** | Typography | Inter (UI text), JetBrains Mono (code display) |

### 2.2 Backend
| Technology | Role | Details |
|---|---|---|
| **Python 3.9** | Core language | All backend modules |
| **FastAPI** | Web framework | REST API with automatic OpenAPI docs |
| **Uvicorn** | ASGI server | High-performance async server |
| **Pydantic** | Data validation | Request/response models |

### 2.3 AI/ML Stack
| Technology | Role | Details |
|---|---|---|
| **BAAI/bge-large-en** | Embedding model | 1024-dim vectors, L2 normalized |
| **sentence-transformers** | Model runtime | PyTorch-based, runs on Apple MPS/GPU |
| **ChromaDB** | Vector database | Persistent, cosine similarity HNSW index |
| **Gemini 2.0 Flash** | LLM (via OpenRouter) | Code generation from context + prompt |

### 2.4 Libraries
| Library | Version | Purpose |
|---|---|---|
| `fastapi` | Latest | REST API framework |
| `uvicorn` | Latest | ASGI server |
| `chromadb` | Latest | Vector database |
| `sentence-transformers` | Latest | Embedding model loader |
| `openai` | Latest | OpenRouter/OpenAI SDK (chat completions) |
| `google-genai` | Latest | Google Gemini SDK (fallback) |
| `python-dotenv` | Latest | Environment variable management |
| `python-multipart` | Latest | File upload support |
| `pydantic` | Latest | Request/response validation |

---

## 3. System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         FRONTEND (Browser)                              │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐                │
│  │  Prompt Input │   │ File Upload  │   │  File Viewer │                │
│  │  (textarea)   │   │ (drag-drop)  │   │  (cards)     │                │
│  └──────┬───────┘   └──────┬───────┘   └──────────────┘                │
│         │                  │                   ▲                        │
│         │    HTTP/JSON     │   multipart/form  │  JSON response         │
└─────────┼──────────────────┼───────────────────┼────────────────────────┘
          │                  │                   │
          ▼                  ▼                   │
┌─────────────────────────────────────────────────────────────────────────┐
│                     FASTAPI SERVER (Port 8000)                          │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                    API LAYER (api/server.py)                      │   │
│  │  POST /generate    POST /ingest    POST /upload-api-docs         │   │
│  │  GET  /health      GET  /collections                             │   │
│  └──────────┬─────────────────────────────────────────────────────┘   │
│             │                                                         │
│             ▼                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                  PIPELINE (pipeline.py)                           │   │
│  │                                                                    │   │
│  │  Step 1: parse_prompt()     → ConnectorSpec                       │   │
│  │  Step 2: retrieve()         → Relevant code chunks                │   │
│  │  Step 3: build_context()    → Assembled context string            │   │
│  │  Step 4: generate()         → LLM-generated code                  │   │
│  │  Step 5: validate()         → Syntax check                        │   │
│  │  Step 6: store_feedback()   → Save for future retrieval           │   │
│  └──────────┬────────┬────────┬────────┬────────────────────────────┘   │
│             │        │        │        │                               │
│             ▼        ▼        ▼        ▼                               │
│  ┌────────────┐ ┌─────────┐ ┌───────────┐ ┌──────────────────────┐    │
│  │  Query     │ │Retrieval│ │ Context   │ │   LLM Generator      │    │
│  │  Processor │ │ Engine  │ │ Builder   │ │  (OpenRouter/Gemini) │    │
│  │            │ │         │ │           │ │                      │    │
│  │ NLP parse  │ │ Embed   │ │ Rank &    │ │ System + User prompt │    │
│  │ → name,   │ │ query → │ │ assemble  │ │ → API call           │    │
│  │   auth,   │ │ cosine  │ │ top chunks│ │ → generated code     │    │
│  │   entities│ │ search  │ │ into text │ │                      │    │
│  └────────────┘ └────┬────┘ └───────────┘ └──────────────────────┘    │
│                      │                                                 │
│                      ▼                                                 │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │              EMBEDDING LAYER (embeddings/embedder.py)            │   │
│  │                                                                    │   │
│  │  Model: BAAI/bge-large-en (sentence-transformers)                │   │
│  │  Dimension: 1024                                                  │   │
│  │  Normalization: L2 (for cosine similarity)                        │   │
│  │  Device: Apple MPS (Metal Performance Shaders GPU)                │   │
│  │                                                                    │   │
│  │  embed_texts(docs)  → batch encode documents                     │   │
│  │  embed_query(query) → single query encode (different prefix)     │   │
│  └──────────┬───────────────────────────────────────────────────────┘   │
│             │                                                         │
│             ▼                                                         │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │              VECTOR DB (vectordb/store.py)                       │   │
│  │                                                                    │   │
│  │  Engine: ChromaDB (PersistentClient)                              │   │
│  │  Index: HNSW with cosine distance metric                          │   │
│  │  Storage: ./chroma_db/ (local disk)                               │   │
│  │                                                                    │   │
│  │  Collections:                                                      │   │
│  │    connector_knowledge  → 5,494 chunks (Java/JS/XML code)        │   │
│  │    nova_framework       → 73 chunks (architecture docs)           │   │
│  │    data_schema          → 28 chunks (VendorConstants, DML)        │   │
│  │    api_docs             → 0+ chunks (user-uploaded API docs)      │   │
│  │    generated_connectors → 9+ chunks (feedback loop)               │   │
│  └──────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Module-by-Module Breakdown

### 4.1 `api/server.py` — FastAPI Web Server
**Role:** HTTP layer connecting the frontend to the backend pipeline.

| Endpoint | Method | Purpose |
|---|---|---|
| `/health` | GET | Server health check, returns embedding model name |
| `/collections` | GET | Returns document counts per ChromaDB collection |
| `/generate` | POST | Triggers the full RAG generation pipeline |
| `/ingest` | POST | Re-ingests SIH codebase into ChromaDB |
| `/upload-api-docs` | POST | Accepts file uploads, ingests into `api_docs` collection |
| `/ingest-framework` | POST | Re-ingests architecture docs into `nova_framework` |
| `/` | GET | Serves the frontend HTML |
| `/static/*` | GET | Serves CSS, JS, and other static assets |

**Key design:** Uses lazy-loaded singletons (`get_embedder()`, `get_store()`) so the embedding model (1.3GB) is loaded once and shared across all requests.

---

### 4.2 `pipeline.py` — End-to-End Orchestrator
**Role:** Coordinates all 6 steps of the generation pipeline.

**Flow:**
```
parse_prompt() → retrieve_for_file_task() × 9 → build_context() → generate() → validate() → store_feedback()
```

For each of the 9 connector files, it:
1. Builds a targeted query based on the file type
2. Retrieves the top-matching code chunks from ChromaDB
3. Assembles them into a context string
4. Sends the context + task to the LLM
5. Validates the generated output
6. Saves to disk

---

### 4.3 `agent/query_processor.py` — Prompt Parser
**Role:** Extracts structured information from the user's natural language prompt.

**Input:** `"Create a Zoom connector with OAuth2 for importing meeting recordings"`

**Output (ConnectorSpec):**
```python
ConnectorSpec(
    connector_name="Zoom",
    auth_type="oauth2",
    entity_types=["content"],
    sync_direction="IMPORT",
    api_base_url="https://api.zoom.us/v2"
)
```

**How it works:** Pattern matching + keyword extraction. Identifies:
- Connector name (e.g., Zoom, Stripe, Udemy)
- Authentication type (oauth2, basic, api_key, session)
- Entity types (content, learningpath, user)
- Sync direction (IMPORT, EXPORT)

---

### 4.4 `embeddings/embedder.py` — Embedding Service
**Role:** Converts text (code, docs, queries) into 1024-dimensional vectors.

**Model:** `BAAI/bge-large-en`
- **Architecture:** BERT-based bi-encoder
- **Dimension:** 1024
- **Training:** Contrastive learning on text pairs
- **Normalization:** L2 normalized (so dot product = cosine similarity)
- **Runtime:** PyTorch on Apple MPS (Metal GPU)

**Two encoding modes:**
| Mode | Prefix | Used For |
|---|---|---|
| `embed_texts()` | `"Represent this sentence: "` | Indexing documents |
| `embed_query()` | `"Represent this sentence for searching relevant passages: "` | Search queries |

This asymmetric prefix is a BGE-specific optimization — the model was trained with different prefixes for documents vs. queries, which improves retrieval accuracy by ~5-8%.

---

### 4.5 `vectordb/store.py` — ChromaDB Vector Store
**Role:** Stores and retrieves document embeddings using cosine similarity.

**Engine:** ChromaDB PersistentClient
- **Index type:** HNSW (Hierarchical Navigable Small World) — approximate nearest neighbor
- **Distance metric:** Cosine (`hnsw:space = "cosine"`)
- **Storage:** Local disk at `./chroma_db/`

**How cosine similarity works:**
```
similarity(A, B) = (A · B) / (||A|| × ||B||)
```
Since our embeddings are L2-normalized (||A|| = ||B|| = 1), this simplifies to just the dot product:
```
similarity(A, B) = A · B
```
ChromaDB returns `distance = 1 - similarity`, so we convert back: `score = 1 - distance`

**Collections:**

| Collection | Documents | Metadata Fields | Content |
|---|---|---|---|
| `connector_knowledge` | 5,494 | `connector`, `component_type`, `file_name` | Java/JS/XML code chunks from all connectors |
| `nova_framework` | 73 | `component_type` | Architecture guides, auth patterns, flow pipeline docs |
| `data_schema` | 28 | `component_type` | VendorConstants, DefaultMappingConfig, DML scripts |
| `api_docs` | 0+ | `component_type` | User-uploaded API documentation |
| `generated_connectors` | 9+ | `connector`, `quality_score` | Previously generated code (feedback loop) |

---

### 4.6 `agent/retriever.py` — Retrieval Engine
**Role:** Performs semantic search across ChromaDB collections with metadata filtering.

**How retrieval works for each file type:**

When generating `ZoomConstants.java`, the retriever runs:

```
Query 1: "Java Constants class defining API configuration keys endpoints headers"
  → Collection: connector_knowledge
  → Filter: component_type = "constants"
  → Result: KalturaConstants.java chunks, LinkedInLearningConstants.java chunks

Query 2: "Zoom API endpoints and configuration"
  → Collection: api_docs
  → Filter: none
  → Result: Any uploaded Zoom API docs
```

Both result sets are merged, deduplicated by ID, and re-ranked by cosine similarity score. The top 15 chunks become context for the LLM.

**File-specific retrieval strategies:**

| File Type | Query Focus | Collections Searched | Metadata Filter |
|---|---|---|---|
| constants | API config keys, endpoints | connector_knowledge, api_docs | `component_type=constants` |
| test_connection | Auth validation, credentials | connector_knowledge, api_docs | `component_type=test_connection` |
| component_control | Pagination, headers, body | connector_knowledge | `component_type=component_control` |
| flows | Spring beans, auth strategy | connector_knowledge | `component_type=flows` |
| flow_definition | HTTP, jsonToXml, XSLT pipeline | connector_knowledge | `component_type=flow_definition` |
| mapping_xml | Field mappings, source-target | connector_knowledge, api_docs | `component_type=mapping` |
| vendor_constants | Integration ID registration | data_schema | `component_type=vendor_constants` |
| mapping_config | Mapping file enum | data_schema | `component_type=mapping_config` |
| dml | SQL registration scripts | data_schema | `component_type=dml` |

---

### 4.7 `agent/context_builder.py` — Context Assembler
**Role:** Takes ranked retrieval results and assembles a coherent context string for the LLM.

Sorts chunks by relevance score, adds source file references, and truncates to fit within the LLM's context window (~30,000 tokens for Gemini 2.0 Flash).

---

### 4.8 `agent/prompt_templates.py` — Prompt Templates
**Role:** Contains the system prompt and per-file task definitions.

**System prompt** instructs the LLM:
- You are a Java developer on the Nova platform
- Follow the exact patterns from the reference code
- Use proper package names, ID formats, bean naming conventions
- Generate only code, no explanations

**Per-file tasks** specify:
- File name to generate (e.g., `ZoomConstants.java`)
- What the file should contain
- Required patterns and conventions

---

### 4.9 `agent/generator.py` — LLM Code Generator
**Role:** Sends the assembled prompt to an LLM and returns generated code.

**Supports 5 backends (in priority order):**

| # | Backend | API | Model | How It's Called |
|---|---|---|---|---|
| 1 | CSOD Gateway | Internal REST | anthropic-sonnet-4.0 | Bearer token auth |
| 2 | **OpenRouter** ✅ | OpenAI-compatible | **google/gemini-2.0-flash-001** | API key → openai SDK |
| 3 | Google Gemini | google-genai SDK | gemini-2.0-flash | API key, with retry on 429 |
| 4 | OpenAI | openai SDK | gpt-4o | API key |
| 5 | Anthropic | REST (raw) | claude-sonnet-4 | x-api-key header |

**Currently active:** OpenRouter → Gemini 2.0 Flash (verified working)

**Post-processing:** Strips markdown code fences (```java ... ```) from LLM output to get clean code.

---

### 4.10 `agent/validator.py` — Code Validator
**Role:** Validates generated code for basic correctness.

| File Type | Validation Checks |
|---|---|
| Java files | Has `package` declaration, has `class`/`interface`, proper braces |
| Content.js | Valid JSON, has `components` and `flow` keys |
| XML files | Valid XML structure |
| SQL files | Has `call` or `INSERT` statements |

---

### 4.11 `agent/feedback.py` — Feedback Loop
**Role:** Stores generated connectors back into ChromaDB so future generations can reference them.

After generating a Zoom connector, the code is embedded and added to the `generated_connectors` collection. If someone later asks for a "Teams connector," the Zoom code may be retrieved as additional context.

---

### 4.12 `ingest/` — Ingestion Pipeline

#### `ingest_connectors.py`
Scans the SIH codebase for all connector files:
- `integration/apps/src/main/java/.../*.java` → Java classes
- `integration/apps/src/main/resources/.../flow/*.js` → Flow definitions
- `integration/apps/src/main/resources/.../default_data/*.xml` → Mappings
- `database/*.sql` → DML scripts

Each file is chunked semantically, embedded, and stored in ChromaDB.

#### `ingest_api_docs.py`
Handles:
1. **User-uploaded API docs** → saved to `knowledge/api_docs/{connector}/`, ingested into `api_docs` collection
2. **Nova framework docs** → architecture guides + Kaltura overview, ingested into `nova_framework` collection

#### `chunker.py`
Splits files into semantic chunks:
- Java: split by class/method boundaries
- JSON: split by top-level keys
- XML: split by elements
- Markdown: split by heading sections

Each chunk gets metadata: `connector`, `component_type`, `file_name`, `language`

---

## 5. Data Flow — End to End

### Phase A: Ingestion (One-time Setup)

```
SIH Codebase (Kaltura, LinkedIn Learning, Udemy, Workday, Zoom...)
    │
    ▼
ingest_connectors.py
    │
    ├── Scan: 50+ files across 10+ connectors
    ├── Chunk: Split into ~5,494 semantic chunks
    ├── Embed: BAAI/bge-large-en → 1024-dim vectors
    └── Store: ChromaDB (connector_knowledge collection)

Architecture Docs (3 guides we wrote)
    │
    ▼
ingest_api_docs.py → ingest_nova_docs()
    │
    ├── Chunk: 73 chunks from 4 documents
    ├── Embed: Same model
    └── Store: ChromaDB (nova_framework collection)
```

### Phase B: Generation (Per Request)

```
User: "Build a Zoom connector with OAuth2 for meeting recordings"
    │
    ▼
┌─── Step 1: Parse Prompt ───────────────────────────────┐
│  Input:  "Build a Zoom connector with OAuth2..."       │
│  Output: ConnectorSpec(name=Zoom, auth=oauth2,         │
│          entities=[content], direction=IMPORT)          │
└────────────────────────────────────────────────────────┘
    │
    ▼ (repeat for each of 9 file types)
    │
┌─── Step 2: Retrieve Context ──────────────────────────┐
│  Query: "Java Constants class API configuration keys"  │
│                    │                                    │
│                    ▼                                    │
│  embed_query() → [0.023, -0.187, 0.456, ..., 0.089]  │
│  (1024-dim vector, L2 normalized)                      │
│                    │                                    │
│                    ▼                                    │
│  ChromaDB.query(embedding, top_k=5,                    │
│                 where={component_type: "constants"})    │
│                    │                                    │
│  HNSW Index → cosine similarity search                 │
│                    │                                    │
│                    ▼                                    │
│  Results:                                               │
│    1. KalturaConstants.java    (score: 0.872)          │
│    2. LinkedInLearningConstants.java (score: 0.845)    │
│    3. UdemyConstants.java      (score: 0.831)          │
│    4. Auth patterns doc chunk  (score: 0.793)          │
│    5. Architecture guide chunk (score: 0.776)          │
└────────────────────────────────────────────────────────┘
    │
    ▼
┌─── Step 3: Build Context ─────────────────────────────┐
│  Assemble top chunks into a single context string:     │
│                                                         │
│  "REFERENCE CODE (KalturaConstants.java, score=0.87):  │
│   package com.saba.integration.apps.kaltura;            │
│   public class KalturaConstants {                       │
│       public static final String BASE_URL = ...         │
│       ...                                               │
│   }                                                     │
│                                                         │
│   REFERENCE CODE (LinkedInLearningConstants.java):      │
│   ..."                                                  │
└────────────────────────────────────────────────────────┘
    │
    ▼
┌─── Step 4: Generate via LLM ──────────────────────────┐
│  System Prompt:                                         │
│    "You are a Java developer. Generate production-     │
│     ready Nova connector code following the exact       │
│     patterns from the reference code..."                │
│                                                         │
│  User Prompt:                                           │
│    Context + Task Description + Previously Generated    │
│                                                         │
│  → OpenRouter API (https://openrouter.ai/api/v1)       │
│  → Model: google/gemini-2.0-flash-001                  │
│  → Response: Generated ZoomConstants.java              │
└────────────────────────────────────────────────────────┘
    │
    ▼
┌─── Step 5: Validate ──────────────────────────────────┐
│  Check: Has "package" declaration? ✅                  │
│  Check: Has "class" keyword? ✅                        │
│  Check: Proper brace matching? ✅                      │
└────────────────────────────────────────────────────────┘
    │
    ▼
┌─── Step 6: Save & Feedback ───────────────────────────┐
│  Write: generated/zoom/ZoomConstants.java              │
│  Embed: generated code → ChromaDB (generated_connectors)│
│  → Available for future retrieval                       │
└────────────────────────────────────────────────────────┘
    │
    ▼
JSON Response → Frontend → Display in file cards
```

---

## 6. AI/ML Models Used

### 6.1 Embedding Model: BAAI/bge-large-en

| Property | Value |
|---|---|
| **Full Name** | BAAI General Embedding Large English |
| **Provider** | Beijing Academy of Artificial Intelligence (BAAI) |
| **Architecture** | BERT-based bi-encoder, 24 layers, 16 attention heads |
| **Parameters** | ~335 million |
| **Embedding Dimension** | 1024 |
| **Max Sequence Length** | 512 tokens |
| **Training** | Contrastive learning on 200M+ text pairs |
| **Normalization** | L2 (unit vectors for cosine similarity) |
| **MTEB Benchmark Rank** | Top-tier for retrieval tasks |
| **Size on Disk** | ~1.3 GB |
| **Runtime** | sentence-transformers + PyTorch |
| **Device** | Apple MPS (Metal Performance Shaders) |

**Why this model?** BGE-large-en is the best open-source English embedding model under 1B parameters. It outperforms OpenAI's text-embedding-ada-002 on most retrieval benchmarks while running locally with no API costs.

### 6.2 LLM: Google Gemini 2.0 Flash (via OpenRouter)

| Property | Value |
|---|---|
| **Model ID** | `google/gemini-2.0-flash-001` |
| **Provider** | Google DeepMind |
| **Access** | Via OpenRouter API (OpenAI-compatible) |
| **Context Window** | 1,048,576 tokens (1M) |
| **Max Output** | 8,192 tokens per request |
| **Temperature** | 0.1 (deterministic for code gen) |
| **Pricing** | ~$0.10/M input, $0.40/M output tokens |
| **Latency** | ~3-8 seconds per file generation |

**Why this model?** Fast, cheap, excellent code generation. The 1M context window means we can feed large amounts of reference code without truncation.

### 6.3 Vector Index: HNSW (ChromaDB default)

| Property | Value |
|---|---|
| **Algorithm** | Hierarchical Navigable Small World |
| **Distance Metric** | Cosine similarity |
| **Approximate** | Yes (not exact, but 95%+ recall) |
| **Complexity** | O(log n) per query |
| **Memory** | In-memory graph + disk persistence |

---

## 7. Frontend ↔ Backend Linkage

### Communication Protocol
- **Transport:** HTTP/1.1
- **Format:** JSON (request and response bodies)
- **File Uploads:** multipart/form-data
- **CORS:** Enabled for all origins (development mode)

### Frontend → Backend Calls

```javascript
// 1. Health Check — verifies server is running
fetch('/health')
  → GET /health
  → Response: { "status": "ok", "model": "BAAI/bge-large-en" }

// 2. Stats — shows collection counts
fetch('/collections')
  → GET /collections
  → Response: { "collections": { "connector_knowledge": 5494, ... } }

// 3. Generate Connector — main flow
fetch('/generate', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    prompt: "Build a Zoom connector with OAuth2",
    skip_validation: false
  })
})
  → POST /generate
  → Triggers: pipeline.run_pipeline()
  → Response: {
      connector_name: "Zoom",
      files: [{ file_name, file_type, content, path, retrieval_count }],
      validation_passed: true,
      spec: { auth_type: "oauth2", entity_types: ["content"] }
    }

// 4. Upload API Docs — file upload with multipart
const formData = new FormData();
formData.append('file', selectedFile);
fetch('/upload-api-docs?connector=udemy', {
  method: 'POST',
  body: formData
})
  → POST /upload-api-docs
  → Saves file to knowledge/api_docs/udemy/
  → Ingests into ChromaDB api_docs collection
  → Response: { status: "ok", connector: "udemy", file: "api.md" }

// 5. Run Ingestion — re-index codebase
fetch('/ingest', {
  method: 'POST',
  body: JSON.stringify({ reset: true })
})
  → POST /ingest
  → Scans SIH codebase, re-chunks, re-embeds, re-stores
  → Response: { status: "ok", stats: {...} }
```

### Backend → Frontend Response Flow

```
Server receives POST /generate
  │
  ├── 1. Initializes EmbeddingService (lazy singleton)
  ├── 2. Initializes VectorStore (lazy singleton)
  ├── 3. Calls pipeline.run_pipeline(prompt, ...)
  │       │
  │       ├── parse_prompt() → ConnectorSpec
  │       ├── For each file_type in [constants, test_connection, ...]:
  │       │     ├── retriever.retrieve_for_file_task()
  │       │     │     ├── embed_query() → 1024-dim vector
  │       │     │     └── chromadb.query() → top-K chunks (cosine)
  │       │     ├── build_context(chunks)
  │       │     ├── generator.generate(system_prompt, user_prompt)
  │       │     │     └── openrouter API call → generated code
  │       │     └── validate_file(code)
  │       │
  │       └── Returns: { connector_name, files[], validation_passed, spec }
  │
  └── Returns JSON to frontend
        │
        ▼
  Frontend renders file cards with expandable code preview
```

---

## 8. API Endpoints Reference

### POST /generate
```json
// Request
{
  "prompt": "Create a Stripe connector with OAuth2 for content sync",
  "file_types": ["constants", "test_connection"],  // optional, default: all 9
  "skip_validation": false
}

// Response
{
  "connector_name": "Stripe",
  "files": [
    {
      "file_name": "StripeConstants.java",
      "file_type": "constants",
      "content": "package com.saba.integration.apps.stripe;\n...",
      "path": "generated/stripe/StripeConstants.java",
      "retrieval_count": 5
    }
  ],
  "validation_passed": true,
  "output_dir": "generated/stripe",
  "spec": {
    "auth_type": "oauth2",
    "entity_types": ["content"],
    "sync_direction": "IMPORT"
  }
}
```

### POST /upload-api-docs
```
Content-Type: multipart/form-data
Query param: connector=udemy
Body: file=@udemy_api.md

Response: { "status": "ok", "connector": "udemy", "file": "udemy_api.md" }
```

### GET /collections
```json
{
  "collections": {
    "connector_knowledge": 5494,
    "nova_framework": 73,
    "data_schema": 28,
    "api_docs": 0,
    "generated_connectors": 9
  }
}
```

---

## 9. Vector Database Design

### Why ChromaDB?
- **Zero infrastructure** — runs embedded in Python, no separate server
- **Persistent** — data survives restarts (stored on disk)
- **HNSW index** — fast approximate nearest neighbor search
- **Metadata filtering** — filter by component_type before similarity search
- **Python-native** — seamless integration, no network overhead

### Collection Schema

```
connector_knowledge:
  ├── id: "kaltura_constants_chunk_0"
  ├── document: "public static final String BASE_URL = ..."
  ├── embedding: [0.023, -0.187, ..., 0.089]  (1024 floats)
  └── metadata:
        ├── connector: "kaltura"
        ├── component_type: "constants"
        ├── file_name: "KalturaConstants.java"
        └── language: "java"
```

### Query Process

```python
# 1. Embed the query
query_vector = embedder.embed_query("Java Constants class API keys")  # → [1024 floats]

# 2. Search with metadata filter
results = collection.query(
    query_embeddings=[query_vector],
    n_results=5,
    where={"component_type": "constants"},  # filter BEFORE similarity
    include=["documents", "metadatas", "distances"]
)

# 3. ChromaDB internally does:
#    a. Filter documents where component_type == "constants"
#    b. Compute cosine distance between query_vector and each filtered doc
#    c. Return top 5 by smallest distance (most similar)

# 4. We convert distance to similarity score
score = 1.0 - distance  # e.g., distance=0.13 → score=0.87
```

---

## 10. RAG Pipeline Deep Dive

### What Makes This "RAG" vs. Plain Prompting?

| Aspect | Plain LLM | Our RAG System |
|---|---|---|
| Context | LLM's training data only | 5,604 real code chunks from SIH |
| Accuracy | Generic patterns | Exact Nova framework patterns |
| IDs | Random or missing | Correct `integ`/`mpent` format |
| Bean names | Generic Spring beans | `integration.{connector}.import.content` |
| Auth | Generic OAuth | Exact `oauth2v2` / `AbstractReusableAuthStrategy` |
| DML | Missing or wrong | Correct `mpp_vendor_entity_ins` calls |

### The "Agentic" Part
The system is "agentic" because it:
1. **Decides** which collections to search based on file type
2. **Adapts** queries based on the target file (different query for Constants vs. DML)
3. **Chains** file generation — later files reference earlier ones (Flows.java references Constants.java)
4. **Self-improves** — stores generated output as future retrieval context

---

## 11. File Structure

```
nova_connector_builder/
├── api/
│   └── server.py                 # FastAPI server (HTTP layer)
├── agent/
│   ├── query_processor.py        # NLP prompt parsing → ConnectorSpec
│   ├── retriever.py              # Semantic search across ChromaDB
│   ├── context_builder.py        # Assembles context from chunks
│   ├── prompt_templates.py       # System prompt + per-file task templates
│   ├── generator.py              # LLM API caller (OpenRouter/Gemini/OpenAI)
│   ├── validator.py              # Code syntax validator
│   └── feedback.py               # Stores output for future retrieval
├── embeddings/
│   └── embedder.py               # BAAI/bge-large-en embedding service
├── vectordb/
│   ├── __init__.py
│   └── store.py                  # ChromaDB vector store (5 collections)
├── ingest/
│   ├── ingest_connectors.py      # SIH codebase scanner + indexer
│   ├── ingest_api_docs.py        # API doc + framework doc indexer
│   └── chunker.py                # Semantic file chunker
├── frontend/
│   ├── index.html                # UI structure
│   ├── index.css                 # Dark premium theme
│   └── app.js                    # Frontend logic (fetch API calls)
├── docs/
│   ├── nova_connector_architecture_guide.md
│   ├── authentication_patterns.md
│   ├── flow_pipeline_architecture.md
│   └── SYSTEM_ARCHITECTURE_COMPLETE.md  (this file)
├── knowledge/
│   ├── api_docs/                 # User-uploaded API documentation
│   └── nova_docs/                # Framework reference docs
├── generated/                    # Output directory for generated connectors
├── chroma_db/                    # ChromaDB persistent storage
├── pipeline.py                   # End-to-end orchestrator
├── app.py                        # CLI interface (alternative to web)
├── .env                          # API keys, model config
└── venv/                         # Python virtual environment
```

---

## 12. Environment Configuration

```bash
# .env file

# OpenRouter API (PRIMARY — currently active)
OPENROUTER_API_KEY=sk-or-v1-...
OPENROUTER_MODEL=google/gemini-2.0-flash-001

# Google Gemini API (backup)
GEMINI_API_KEY=AIzaSy...
GEMINI_MODEL=gemini-2.0-flash

# OpenAI API (backup)
OPENAI_API_KEY=sk-proj-...
OPENAI_MODEL=gpt-4o

# CSOD Internal Gateway (enterprise)
LLM_GATEWAY_URL=https://dev01-llm-platform.itf.csodqa.com/v3/generate
LLM_GATEWAY_TOKEN=
LLM_GATEWAY_MODEL=anthropic-sonnet-4.0

# Embedding Model
EMBEDDING_MODEL=BAAI/bge-large-en

# ChromaDB
CHROMA_PERSIST_DIR=./chroma_db

# SIH Codebase (for ingestion)
SIH_CODEBASE_PATH=../

# Output
OUTPUT_DIR=./generated
```

---

## 13. How to Run

### First Time Setup
```bash
cd nova_connector_builder
python -m venv venv
source venv/bin/activate
pip install fastapi uvicorn chromadb sentence-transformers openai google-genai python-dotenv python-multipart

# Configure .env with at least one API key
# Then ingest the knowledge base:
python ingest/ingest_connectors.py --reset
```

### Start the Server
```bash
source venv/bin/activate
python api/server.py
# → http://localhost:8000
```

### Generate a Connector
1. Open http://localhost:8000
2. (Optional) Upload target API docs in the upload section
3. Type: "Create a Stripe connector with OAuth2 for content sync"
4. Click "⚡ Generate Connector"
5. 9 files appear on the right panel in ~30-60 seconds

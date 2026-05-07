"""
api/server.py
FastAPI server for the Nova Connector Builder.
Run from project root: python -m api.server  OR  python api/server.py
"""

import os
import sys
import logging
from pathlib import Path
from typing import List, Optional

# Ensure project root is on path
PROJECT_ROOT = str(Path(__file__).resolve().parent.parent)
if PROJECT_ROOT not in sys.path:
    sys.path.insert(0, PROJECT_ROOT)

from fastapi import FastAPI, HTTPException, UploadFile, File
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse, HTMLResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from dotenv import load_dotenv

load_dotenv(os.path.join(PROJECT_ROOT, ".env"))

logger = logging.getLogger(__name__)

app = FastAPI(
    title="Nova Connector Builder API",
    description="RAG-powered connector code generation for the Nova integration platform",
    version="1.0.0",
)

# CORS — allow frontend on any origin during dev
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Shared services (lazy init)
_embedder = None
_store = None


def get_embedder():
    global _embedder
    if _embedder is None:
        from embeddings.embedder import EmbeddingService
        _embedder = EmbeddingService()
    return _embedder


def get_store():
    global _store
    if _store is None:
        from vectordb.store import VectorStore
        _store = VectorStore()
    return _store


# ── Request / Response Models ──


class GenerateRequest(BaseModel):
    prompt: str
    file_types: Optional[List[str]] = None
    skip_validation: bool = False


class GenerateResponse(BaseModel):
    connector_name: str
    files: List[dict]
    validation_passed: bool
    output_dir: str
    spec: dict


class IngestRequest(BaseModel):
    connectors: Optional[List[str]] = None
    reset: bool = False


class StatsResponse(BaseModel):
    collections: dict


# ── Endpoints ──


@app.get("/health")
def health():
    return {"status": "ok", "model": os.getenv("EMBEDDING_MODEL", "BAAI/bge-large-en")}


@app.get("/collections", response_model=StatsResponse)
def get_collections():
    store = get_store()
    return StatsResponse(collections=store.get_stats())


@app.post("/generate", response_model=GenerateResponse)
def generate(req: GenerateRequest):
    from pipeline import run_pipeline

    try:
        result = run_pipeline(
            prompt=req.prompt,
            file_types=req.file_types,
            skip_validation=req.skip_validation,
            embedder=get_embedder(),
            store=get_store(),
        )
        return GenerateResponse(
            connector_name=result["connector_name"],
            files=result["files"],
            validation_passed=result["validation_passed"],
            output_dir=result["output_dir"],
            spec=result["spec"],
        )
    except Exception as e:
        logger.error(f"Generation error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/ingest")
def ingest(req: IngestRequest):
    from ingest.ingest_connectors import ingest_all

    try:
        stats = ingest_all(
            embedder=get_embedder(),
            store=get_store(),
            connectors=req.connectors,
            reset=req.reset,
        )
        return {"status": "ok", "stats": stats}
    except Exception as e:
        logger.error(f"Ingestion error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/upload-api-docs")
async def upload_api_docs(connector: str, file: UploadFile = File(...)):
    """Upload API documentation for a target connector and ingest into ChromaDB."""
    from ingest.ingest_api_docs import ingest_api_docs

    # Save uploaded file
    docs_dir = os.path.join(PROJECT_ROOT, "knowledge", "api_docs", connector.lower())
    os.makedirs(docs_dir, exist_ok=True)
    file_path = os.path.join(docs_dir, file.filename)

    content = await file.read()
    with open(file_path, "wb") as f:
        f.write(content)

    # Ingest into ChromaDB
    try:
        ingest_api_docs(
            connector_name=connector.lower(),
            embedder=get_embedder(),
            store=get_store(),
            reset=False,
        )
        return {
            "status": "ok",
            "connector": connector,
            "file": file.filename,
            "message": f"Uploaded and ingested {file.filename} for {connector}",
        }
    except Exception as e:
        logger.error(f"API docs upload error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/ingest-framework")
def ingest_framework():
    """Re-ingest Nova framework docs (architecture guides)."""
    from ingest.ingest_api_docs import ingest_nova_docs

    try:
        ingest_nova_docs(embedder=get_embedder(), store=get_store())
        stats = get_store().get_stats()
        return {"status": "ok", "stats": stats}
    except Exception as e:
        logger.error(f"Framework ingestion error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# ── Repo Integration ──

SIH_CODEBASE_PATH = os.path.abspath(os.path.join(PROJECT_ROOT, os.getenv("SIH_CODEBASE_PATH", "..")))


class ApplyRequest(BaseModel):
    connector_name: str
    generated_files: list


@app.post("/preview-apply")
def preview_apply_endpoint(req: ApplyRequest):
    """Preview what will change when applying generated files to the SIH repo."""
    from agent.repo_integrator import preview_apply

    try:
        actions = preview_apply(req.connector_name, req.generated_files, SIH_CODEBASE_PATH)
        # Don't send full content to frontend — just metadata
        preview = []
        for a in actions:
            preview.append({
                "file_type": a["file_type"],
                "file_name": a["file_name"],
                "repo_path": a["repo_path"],
                "action": a["action"],
                "exists": a["exists"],
                "description": a["description"],
                "is_patch": a["is_patch"],
            })
        return {"status": "ok", "repo_path": SIH_CODEBASE_PATH, "actions": preview}
    except Exception as e:
        logger.error(f"Preview error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/apply-to-repo")
def apply_to_repo_endpoint(req: ApplyRequest):
    """Apply generated connector files to the actual SIH repository."""
    from agent.repo_integrator import apply_to_repo

    try:
        result = apply_to_repo(req.connector_name, req.generated_files, SIH_CODEBASE_PATH)
        return {"status": "ok", "repo_path": SIH_CODEBASE_PATH, **result}
    except Exception as e:
        logger.error(f"Apply error: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


# ── Frontend serving ──
# Serve the root "/" as HTML, and "/static/*" for CSS/JS assets

FRONTEND_DIR = os.path.join(PROJECT_ROOT, "frontend")


@app.get("/", response_class=HTMLResponse)
def serve_frontend():
    index_path = os.path.join(FRONTEND_DIR, "index.html")
    if os.path.exists(index_path):
        return HTMLResponse(content=open(index_path).read())
    return HTMLResponse(content="<h1>Nova Connector Builder API</h1><p>Frontend not found.</p>")


# Mount static files AFTER the "/" route so it doesn't override it
if os.path.exists(FRONTEND_DIR):
    app.mount("/static", StaticFiles(directory=FRONTEND_DIR), name="static")


if __name__ == "__main__":
    import uvicorn

    logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
    logger.info(f"Project root: {PROJECT_ROOT}")
    logger.info(f"Frontend dir: {FRONTEND_DIR}")
    logger.info(f"Starting server at http://localhost:8000")
    uvicorn.run(app, host="0.0.0.0", port=8000)

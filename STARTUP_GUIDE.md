# Nova Connector Builder - Startup Guide

If you have just extracted this project from a ZIP archive, follow these steps to get the full application (Backend + Frontend) running on your local machine.

## Prerequisites
- Python 3.9+ installed
- Node.js / npm installed (Optional, for frontend development only)
- Access to the main `sih_main` codebase (if you plan to re-ingest data)

---

## Step 1: Set up the Python Environment

First, open your terminal, navigate to the extracted `nova_connector_builder` folder, and set up a virtual environment to install all dependencies.

```bash
# 1. Navigate to the project root
cd /path/to/extracted/nova_connector_builder

# 2. Create a virtual environment
python3 -m venv venv

# 3. Activate the virtual environment
source venv/bin/activate   # On Mac/Linux
# venv\Scripts\activate    # On Windows

# 4. Install required packages
pip install -r requirements.txt
```

---

## Step 2: Configure Environment Variables

The application needs API keys to communicate with the LLMs (like OpenRouter, Gemini, or Groq). 

1. Locate the `.env.example` file in the root directory.
2. Duplicate it and rename the copy to `.env`.
3. Open `.env` and ensure you have an active API key uncommented (e.g., `OPENROUTER_API_KEY`).

```bash
cp .env.example .env
```

---

## Step 3: (Optional) Rebuild the Vector Database

> [!NOTE]
> If your ZIP file already included the `chroma_db` folder, you can skip this step. The LLM will use the existing memory.

If you need to rebuild the Knowledge Base from scratch, run the ingestion scripts. Make sure your `.env` file has `SIH_CODEBASE_PATH` pointing to the main `sih_main` repo.

```bash
# Make sure your virtual environment is active!
source venv/bin/activate

# Step A: Ingest the Framework Rules and Golden References
python3 ingest/ingest_framework.py --reset

# Step B: Ingest the Codebase Connectors (Takes ~15 mins)
python3 ingest/ingest_connectors.py
```

---

## Step 4: Start the Application (Frontend + Backend)

The Nova Connector Builder is designed so that the **FastAPI backend also serves the HTML/JS frontend**. Because of this, you only need to run **one command** to start the entire application!

Make sure your terminal is located in the **root directory** (`nova_connector_builder`) and your virtual environment is activated, then run:

```bash
# Ensure you are in the root directory:
cd /path/to/nova_connector_builder

# Start the unified server:
python3 api/server.py
```

### Accessing the App:
Once the server says `Application startup complete`, open your browser and go to:
**http://localhost:8000/**

* That URL serves the interactive UI where you can type your prompts.
* All API calls automatically route to `http://localhost:8000/generate` etc.

> [!TIP]
> If you make changes to the frontend code (`frontend/app.js` or `frontend/index.html`), simply refresh your browser. You do not need to restart the Python server unless you change Python files!

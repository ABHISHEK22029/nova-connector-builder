# 🚀 Nova Connector Builder: Complete Boot-Up Walkthrough

Welcome to the Nova Connector Builder! This guide will walk you through exactly how to extract the project, set up your local environment, and boot up the system from scratch.

> [!IMPORTANT]
> The application is cross-platform and will work on Windows, macOS, or Linux. Please ensure you have **Python 3.9 or higher** installed before beginning.

---

## Step 1: Extract the Project

When you receive the `nova_connector_builder_export.zip` file, your first step is to unzip it into a permanent location on your computer.

1. **Locate the ZIP file** in your Downloads folder.
2. **Extract/Unzip** the file:
   - *Windows:* Right-click the `.zip` file and select **Extract All...**
   - *Mac:* Double-click the `.zip` file to expand it.
3. Move the extracted `nova_connector_builder` folder to a dedicated workspace (e.g., your `Documents` folder).

---

## Step 2: Open Your Terminal

You will need to use a command line interface to start the application.

- **Windows:** Open the Start Menu, type `cmd` or `PowerShell`, and hit Enter.
- **Mac:** Open Spotlight Search (Cmd + Space), type `Terminal`, and hit Enter.

Use the `cd` (change directory) command to navigate into the folder you just extracted:

```bash
# Example (Change this path to wherever you extracted the folder!)
cd Documents/nova_connector_builder
```

---

## Step 3: Set Up the Python Environment

To ensure the application has exactly what it needs to run without interfering with other software on your computer, we must create a "Virtual Environment".

Run the following command to create the environment:
```bash
python3 -m venv venv
```
*(Note: If you are on Windows, you may just need to type `python -m venv venv`)*

### Activate the Environment
You must activate this environment every time you want to boot up the system.

- **Mac/Linux:**
  ```bash
  source venv/bin/activate
  ```
- **Windows:**
  ```bash
  venv\Scripts\activate
  ```

> [!TIP]
> You will know it worked if your terminal prompt changes to show `(venv)` at the beginning of the line!

### Install Dependencies
With the environment activated, tell Python to download all the required packages:
```bash
pip install -r requirements.txt
```
*(This may take a minute or two to download everything.)*

---

## Step 4: Configure the API Key

The Nova Connector Builder uses advanced AI models to write code, which requires an API key.

1. Inside the `nova_connector_builder` folder, you will see a file named `.env.example`.
2. **Duplicate this file** and rename the copy to exactly `.env` (with the period at the front).
3. Open `.env` in any text editor (like Notepad or VS Code).
4. Find the line that says `OPENROUTER_API_KEY=` and paste in your active API key. Save and close the file.

---

## Step 5: Boot Up the Application!

You are now ready to start the system. The Nova Connector Builder is uniquely designed so that one single command will start both the backend AI engine and the frontend User Interface.

Make sure your terminal is inside the `nova_connector_builder` directory and your `(venv)` is activated, then run:

```bash
python3 api/server.py
```

### Accessing the Interface
Wait a few seconds until you see `Application startup complete` in your terminal. 

Open your favorite web browser (Chrome, Edge, Safari) and navigate to:
**[http://localhost:8000/](http://localhost:8000/)**

> [!SUCCESS]
> **Congratulations!** You should now see the dark-mode Nova Connector Builder UI. You are ready to start generating integrations!

---

## 🛑 Troubleshooting

- **"Command not found: python3"**: Try typing `python` instead of `python3`. If it still fails, you need to download and install Python from Python.org.
- **"Application failed to start" / LLM Errors**: Double check that your `.env` file was renamed correctly and that your API key is valid.
- **Missing Knowledge Base**: If the AI seems to be hallucinating, you may need to re-index the ChromaDB database. You can do this by opening a new terminal, activating the `venv`, and running `python3 ingest/ingest_connectors.py`.

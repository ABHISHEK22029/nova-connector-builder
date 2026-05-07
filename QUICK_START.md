# Nova Connector Builder - Quick Start

Follow these 3 simple steps to boot up the application:

### 1. Extract the ZIP
Unzip `nova_connector_builder_export.zip` and open your terminal. Navigate into the extracted folder:
```bash
cd path/to/nova_connector_builder
```

### 2. Set up the Environment
Run these commands to install the required Python dependencies:
```bash
python3 -m venv venv
source venv/bin/activate      # Use `venv\Scripts\activate` if on Windows
pip install -r requirements.txt
```

### 3. Start the Application
Boot up the backend API and frontend UI with one command:
```bash
python3 api/server.py
```

That's it! Open your browser to **[http://localhost:8000/](http://localhost:8000/)** to use the application.

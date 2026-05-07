// Nova Connector Builder — Frontend JavaScript

const API_BASE = window.location.origin;

// ── State ──
let selectedFiles = [];

// ── API Calls ──

async function apiCall(endpoint, method = 'GET', body = null) {
    const opts = {
        method,
        headers: { 'Content-Type': 'application/json' },
    };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(`${API_BASE}${endpoint}`, opts);
    if (!res.ok) {
        const err = await res.json().catch(() => ({ detail: res.statusText }));
        throw new Error(err.detail || 'API error');
    }
    return res.json();
}

// ── Upload Section ──

function toggleUpload() {
    const body = document.getElementById('uploadBody');
    const chevron = document.getElementById('uploadChevron');
    body.classList.toggle('hidden');
    chevron.classList.toggle('open');
}

function initUpload() {
    const dropZone = document.getElementById('dropZone');
    const fileInput = document.getElementById('fileInput');
    if (!dropZone || !fileInput) return;

    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.classList.add('dragover');
    });
    dropZone.addEventListener('dragleave', () => dropZone.classList.remove('dragover'));
    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('dragover');
        handleFiles(e.dataTransfer.files);
    });
    fileInput.addEventListener('change', (e) => handleFiles(e.target.files));
}

function handleFiles(fileList) {
    for (const file of fileList) {
        if (!selectedFiles.find(f => f.name === file.name)) selectedFiles.push(file);
    }
    renderFileChips();
    updateUploadBtn();
}

function removeFile(index) {
    selectedFiles.splice(index, 1);
    renderFileChips();
    updateUploadBtn();
}

function renderFileChips() {
    const container = document.getElementById('uploadedFiles');
    container.innerHTML = selectedFiles.map((f, i) => `
        <div class="file-chip">📄 ${f.name} <span class="remove-chip" onclick="removeFile(${i})">✕</span></div>
    `).join('');
}

function updateUploadBtn() {
    const btn = document.getElementById('uploadBtn');
    const connector = document.getElementById('uploadConnector').value.trim();
    btn.disabled = selectedFiles.length === 0 || !connector;
}

async function uploadDocs() {
    const connector = document.getElementById('uploadConnector').value.trim();
    if (!connector || selectedFiles.length === 0) return;

    const btn = document.getElementById('uploadBtn');
    const status = document.getElementById('uploadStatus');
    btn.disabled = true;
    btn.textContent = '⏳ Uploading...';
    status.classList.add('hidden');

    let successCount = 0, errorMsg = '';
    for (const file of selectedFiles) {
        try {
            const formData = new FormData();
            formData.append('file', file);
            const res = await fetch(`${API_BASE}/upload-api-docs?connector=${encodeURIComponent(connector)}`, { method: 'POST', body: formData });
            if (!res.ok) throw new Error((await res.json().catch(() => ({}))).detail || 'Upload failed');
            successCount++;
        } catch (e) { errorMsg = e.message; }
    }

    status.classList.remove('hidden', 'success', 'error');
    if (successCount === selectedFiles.length) {
        status.classList.add('success');
        status.textContent = `✅ ${successCount} file${successCount > 1 ? 's' : ''} uploaded & ingested for "${connector}"`;
        selectedFiles = [];
        renderFileChips();
        if (!document.getElementById('statsPanel').classList.contains('hidden')) loadStats();
    } else {
        status.classList.add('error');
        status.textContent = `❌ ${successCount}/${selectedFiles.length} uploaded. Error: ${errorMsg}`;
    }
    btn.textContent = '📤 Upload & Ingest';
    updateUploadBtn();
}

// ── Stats ──

async function loadStats() {
    const panel = document.getElementById('statsPanel');
    const content = document.getElementById('statsContent');
    panel.classList.toggle('hidden');

    if (!panel.classList.contains('hidden')) {
        try {
            const data = await apiCall('/collections');
            let total = 0;
            const rows = Object.entries(data.collections).map(([name, count]) => {
                total += count;
                return `<div class="stat-row"><span>${name.replace(/_/g, ' ')}</span><span class="stat-value">${count.toLocaleString()}</span></div>`;
            }).join('');
            content.innerHTML = rows + `<div class="stat-row" style="border-top:1px solid var(--border-color);margin-top:4px;padding-top:8px;font-weight:600"><span>Total chunks</span><span class="stat-value">${total.toLocaleString()}</span></div>`;
        } catch (e) {
            content.innerHTML = `<p style="color:var(--error)">Error: ${e.message}</p>`;
        }
    }
}

// ── Ingestion ──

async function runIngestion() {
    const btn = document.getElementById('ingestBtn');
    btn.disabled = true;
    btn.textContent = '⏳ Ingesting...';
    try {
        await apiCall('/ingest', 'POST', { reset: true });
        btn.textContent = '✅ Done!';
        setTimeout(() => { btn.textContent = '📥 Ingest'; btn.disabled = false; }, 2000);
        if (!document.getElementById('statsPanel').classList.contains('hidden')) loadStats();
    } catch (e) {
        btn.textContent = '❌ Failed';
        setTimeout(() => { btn.textContent = '📥 Ingest'; btn.disabled = false; }, 2000);
        alert('Ingestion failed: ' + e.message);
    }
}

// ── Generation ──

async function generate() {
    const prompt = document.getElementById('promptInput').value.trim();
    if (!prompt) { alert('Please enter a connector description'); return; }

    const btn = document.getElementById('generateBtn');
    const progress = document.getElementById('progressPanel');
    const progressFill = document.getElementById('progressFill');
    const progressText = document.getElementById('progressText');
    const output = document.getElementById('outputContainer');

    btn.disabled = true;
    progress.classList.remove('hidden');
    progressFill.style.width = '10%';
    progressFill.style.background = 'linear-gradient(90deg, var(--accent), #8b5cf6)';
    progressText.textContent = 'Parsing prompt...';

    let progressValue = 10;
    const steps = [
        'Retrieving context from knowledge base...',
        'Embedding query with BGE-large...',
        'Building context from 5,500+ chunks...',
        'Generating Constants.java...',
        'Generating TestConnection.java...',
        'Generating ComponentControl.java...',
        'Generating Flows.java...',
        'Generating Content.js flow definition...',
        'Generating XML mapping...',
        'Generating DML script...',
        'Validating generated code...',
    ];
    let stepIdx = 0;
    const progressTimer = setInterval(() => {
        progressValue = Math.min(progressValue + 4, 90);
        progressFill.style.width = progressValue + '%';
        progressText.textContent = steps[stepIdx % steps.length];
        stepIdx++;
    }, 3000);

    try {
        const data = await apiCall('/generate', 'POST', {
            prompt,
            skip_validation: document.getElementById('skipValidation').checked,
        });
        clearInterval(progressTimer);
        progressFill.style.width = '100%';
        progressText.textContent = '✅ Generation complete!';
        renderOutput(data);
    } catch (e) {
        clearInterval(progressTimer);
        progressFill.style.width = '100%';
        progressFill.style.background = 'var(--error)';
        progressText.textContent = '❌ ' + e.message;
        output.innerHTML = `<div class="empty-state"><p style="color:var(--error)">Error: ${e.message}</p></div>`;
    } finally {
        btn.disabled = false;
        setTimeout(() => progress.classList.add('hidden'), 5000);
    }
}

// ── Render Output ──

function renderOutput(data) {
    const output = document.getElementById('outputContainer');
    const validationHtml = `
        <div class="validation-summary">
            ${data.validation_passed ? '✅' : '❌'} 
            <strong>${data.connector_name}</strong> — 
            ${data.files.length} files generated
            (Auth: ${data.spec.auth_type}, Entities: ${data.spec.entity_types.join(', ')})
        </div>`;

    const filesHtml = data.files.map((f, i) => `
        <div class="file-card" id="file-${i}">
            <div class="file-header" onclick="toggleFile(${i})">
                <span class="file-name">${f.file_name}</span>
                <div class="file-meta">
                    <span>${f.file_type}</span>
                    <span>${(f.content.length / 1024).toFixed(1)}KB</span>
                    <span class="file-status">${f.content.includes('GENERATION FAILED') ? '❌' : '✅'}</span>
                </div>
            </div>
            <div class="file-content" id="file-content-${i}">
                <pre>${escapeHtml(f.content)}</pre>
            </div>
        </div>`).join('');

    output.innerHTML = validationHtml + filesHtml;
}

function toggleFile(index) {
    document.getElementById(`file-content-${index}`).classList.toggle('open');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ── Init ──

document.addEventListener('DOMContentLoaded', () => {
    apiCall('/health')
        .then(() => { document.getElementById('statusDot').style.background = 'var(--success)'; })
        .catch(() => { document.getElementById('statusDot').style.background = 'var(--error)'; });
    initUpload();
    const ci = document.getElementById('uploadConnector');
    if (ci) ci.addEventListener('input', updateUploadBtn);
});

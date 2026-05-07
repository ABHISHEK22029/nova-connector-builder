# Flow Pipeline Architecture — Content.js Design Guide

## PURPOSE
This document explains the Content.js flow definition in exhaustive detail. This is the MOST COMPLEX file in a connector and requires careful construction.

---

## 1. OVERVIEW

Content.js defines a **data pipeline** as a directed acyclic graph (DAG) of components. Each component does one transformation step:

```
SOURCE → HTTP → FileProcessor → JsonToXml → XSLT → FileRename → MetaNode → Router
                                                                            ↓
                                                                    (OUTPUT to Nova)
```

Every step has **success** and **failure** paths. Failure paths route to monitoring components that log errors.

---

## 2. COMPONENT TYPES REFERENCE

### 2.1 HTTP Component (REQUIRED)
Fetches data from the vendor API. This is always the FIRST component.

```json
{
  "type": "http",
  "name": "{CONNECTOR}_Content_Http_Component",
  "control": {
    "type": "{connector}ComponentControl",
    "url": "sabaspel:payload.accountConfigs['BASE_URL'][0] + '/api/content?page=' + #currentPageNumber + '&page_size=100'"
  },
  "requestMethod": "GET",
  "headers": {
    "Accept": ["application/json"],
    "Accept-Encoding": ["gzip"]
  },
  "outputType": "FILE",
  "maxLoopCounter": "sabaspel:headers.headers['isPreview']==true ? '1' : '-1'",
  "responseValidator": {
    "type": "noMoreRecords",
    "recordQName": {
      "namespaceURI": "",
      "localPart": "elements"
    },
    "requiredDepth": 2,
    "json": true
  },
  "authenticationStrategy": { ... },
  "retryOptions": {
    "maxRetry": 3,
    "delaySeconds": 5
  },
  "dataPrefix": "{\"Records\":",
  "dataSuffix": "}",
  "jsonforStax": "true"
}
```

**Key fields:**
- `control.url`: The API URL with pagination. Use `#currentPageNumber` for page-based.
- `maxLoopCounter`: `-1` = paginate until no more records. `1` = preview mode (single page).
- `responseValidator.recordQName.localPart`: The JSON key containing the array of records (e.g., `"elements"`, `"results"`, `"data"`, `"items"`).
- `dataPrefix`/`dataSuffix`: Wraps the response for XML conversion. Usually `{"Records":` + `}`.

### 2.2 File Path Processor
Sets up output file paths for intermediate files.

```json
{
  "type": "filePathProcessor",
  "name": "{CONNECTOR}_Content_File_Processor",
  "delimiter": "_",
  "filePathEntries": {
    "com.saba.jsontoxml.outputfile": "sabaspel:integrationMeta.tenant + '_' + headers.headers['INTEGRATION_MONITORING_ID'] + '_' + 'json.xml'",
    "com.saba.xslt.output.filepath": "sabaspel:integrationMeta.tenant + '_' + headers.headers['INTEGRATION_MONITORING_ID'] + '_' + 'transformed.csv'",
    "com.saba.xml.split.output.filepath": "sabaspel:integrationMeta.tenant + '_' + headers.headers['INTEGRATION_MONITORING_ID'] + '_' + 'split.xml'"
  }
}
```

### 2.3 JSON to XML Converter
Converts the JSON API response into XML for XSLT processing.

```json
{
  "type": "jsonToXml",
  "name": "{CONNECTOR}_Content_JsonToXml",
  "eventProcessor": {
    "type": "xmlTagRename",
    "replacements": [
      {
        "source": { "namespaceURI": "", "localPart": "elements" },
        "replacement": { "namespaceURI": "", "localPart": "Record" },
        "depth": 2
      }
    ]
  },
  "outputType": "FILE"
}
```

**CRITICAL:** The `source.localPart` must match the JSON array key from the API response. The `replacement.localPart` is always `"Record"`.

### 2.4 XSLT Transformer Property
Sets up XSLT configuration.

```json
{
  "type": "xsltTransformerProperty",
  "name": "{CONNECTOR}_Content_Xslt_Property"
}
```

### 2.5 XSLT Transformer
Applies the XML mapping to transform vendor data into EdCast format.

```json
{
  "type": "xslt",
  "name": "{CONNECTOR}_Content_Xslt",
  "xmlSplitConfig": {
    "qName": { "namespaceURI": "", "localPart": "Record" },
    "recordsPerFile": 400,
    "includeFirstFileWithCommonEvents": true
  },
  "outputType": "FILE",
  "xslFilePath": "sabaspel:headers.headers['integrationDataFetchDetail'].xslFilePath",
  "properties": {
    "includeHeadersForConfigMapping": "sabaspel:(#loopCounter ==0) ? 'true' : 'false'"
  },
  "appendMode": true
}
```

### 2.6 File Rename
Renames output files for the import step.

```json
{
  "type": "fileNameRename",
  "name": "{CONNECTOR}_Content_FileNameRename",
  "fileName": "{Connector}Catalog",
  "suffix": ".csv"
}
```

### 2.7 Exception Handler
Catches and handles errors.

```json
{
  "type": "exception",
  "name": "{CONNECTOR}_Content_Exception"
}
```

### 2.8 Preview Filter
Filters records for preview mode.

```json
{
  "type": "previewFilter",
  "name": "{CONNECTOR}_PreviewFilter"
}
```

### 2.9 XML Merger
Merges multiple XML fragments.

```json
{
  "type": "xmlMerger",
  "name": "{CONNECTOR}_XmlMerger",
  "outputType": "FILE",
  "xmlAppenderQName": { "namespaceURI": "", "localPart": "Record" }
}
```

### 2.10 File Copy
Copies output files to storage.

```json
{
  "type": "fileCopy",
  "name": "{CONNECTOR}_File_Copy",
  "destinationPath": "sabaspel:headers.headers['storagePath']",
  "fileName": "sabaspel:headers.headers['storageFileName']",
  "fileSuffix": ".xml",
  "removeInputFiles": "true"
}
```

### 2.11 Data Fetch Response Transformer
Signals that data fetch is complete.

```json
{
  "type": "dataFetchResponseTransformer",
  "name": "{CONNECTOR}_MetaNode",
  "event": "FETCH_COMPLETE",
  "eventType": "ALL",
  "status": "SUCCESS"
}
```

### 2.12 Router
Final component — routes the result.

```json
{
  "type": "router",
  "name": "{CONNECTOR}_Router"
}
```

### 2.13 Flow Monitoring
Logs progress and errors. One COMPLETE and one FAILURE per major step.

```json
{
  "type": "flowMonitoring",
  "name": "{CONNECTOR}_HttpMonitoring",
  "detail": {
    "defaultMessage": "{Connector} content Get Data is complete",
    "status": "COMPLETE",
    "details": { "STEP_COMPLETED": "2" }
  }
}
```

```json
{
  "type": "flowMonitoring",
  "name": "{CONNECTOR}_HttpException",
  "detail": {
    "defaultMessage": "{Connector} content Get Data resulted in failure",
    "status": "FAILURE",
    "details": {
      "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
      "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
      "STEP_COMPLETED": "1"
    }
  }
}
```

---

## 3. FLOW ROUTING RULES

The `"flow"` section defines the DAG edges as arrays of component names.

### Success Paths
```json
"success": [
  // Path 1: Main pipeline
  ["SOURCE", "Http", "HttpMonitoring", "FileProcessor", "JsonToXml", "JsonMonitoring", "XsltProperty", "Xslt", "XsltMonitoring", "CsvProcessor"],
  // Path 2: No records
  ["CsvProcessor", "NoRecordsFilter", "NoRecordsMonitoring", "NoRecordsMeta", "Router"],
  // Path 3: Has records
  ["CsvProcessor", "HasRecordsFilter", "FileRename", "EntityConfigUpdater", "MetaNode", "Router"],
  // Path 4: Preview
  ["JsonToXml", "PreviewFilter", "XmlMerger", "FileCopy"],
  // Path 5-N: Error routing
  ["HttpException", "Exception"],
  ["JsonException", "Exception"],
  ["XsltException", "Exception"],
  ["Exception", "Router"]
]
```

### Failure Paths
```json
"failure": [
  ["Http", "HttpException"],
  ["JsonToXml", "JsonException"],
  ["Xslt", "XsltException"]
]
```

**Rule:** Every component that can fail MUST have a failure path to its exception monitoring component.

---

## 4. PAGINATION PATTERNS

### Offset-based
```
url + '&start=' + ((#currentPageNumber - 1) * 100 + 1) + '&count=100'
```

### Page-based
```
url + '&page=' + #currentPageNumber + '&page_size=100'
```

### Cursor-based
Handle in ComponentControl.java by reading cursor from previous response headers.

# Nova Integration Connector Architecture — Complete Developer Guide

## PURPOSE
This document describes the full architecture and file-by-file structure for building a content sync connector on the Nova (Cornerstone/EdCast) integration platform. Any AI model or developer reading this document should be able to produce production-ready connector boilerplate for ANY third-party content provider (e.g., Stripe, Udemy, Workday, Zoom, Coursera, etc.).

---

## 1. WHAT IS A CONNECTOR?

A **connector** is a set of Java classes, JSON flow definitions, XML mapping files, and SQL registration scripts that allow the Nova platform to pull (IMPORT) content from a third-party API and map it into the internal EdCast content schema.

Every connector has **exactly these files**, no more, no less:

| # | File | Language | Purpose |
|---|---|---|---|
| 1 | `{Connector}Constants.java` | Java | Configuration keys, API endpoints, static values |
| 2 | `{Connector}TestConnection.java` | Java | Validates credentials by hitting the vendor API |
| 3 | `{Connector}ComponentControl.java` | Java | HTTP request lifecycle (headers, pagination, body) |
| 4 | `{Connector}Flows.java` | Java | Spring @Configuration defining flow beans + auth strategy |
| 5 | `Content.js` | JSON | Data pipeline definition (fetch → transform → output) |
| 6 | `{connector}_edcast_content.xml` | XML | Field mappings from vendor API to EdCast schema |
| 7 | VendorConstants patch | Java | Integration ID registration |
| 8 | DefaultMappingConfig patch | Java | Mapping file registration |
| 9 | DML Script | SQL | Database registration of vendor, entity, and associations |

---

## 2. AUTHENTICATION PATTERNS

The platform supports multiple authentication strategies. Choose one based on the vendor API:

### 2.1 OAuth2 Client Credentials (LinkedIn Learning)
- Used when the API issues access tokens via `client_id` and `client_secret`.
- Token is exchanged at a token URL and cached via `cachedHeaderProperty`.
- Configuration is embedded directly in the Content.js `authenticationStrategy` block.

**Example (from LinkedIn Learning Content.js):**
```json
"authenticationStrategy": {
  "type": "oauth2v2",
  "detail": {
    "invalidateAfterSeconds": 0,
    "retryCount": 0,
    "sendClientCredentialsInBody": "true",
    "clientId": "sabaspel:payload.accountConfigs['CLIENT_ID'][0]",
    "clientSecret": "sabaspel:payload.accountConfigs['CLIENT_SECRET'][0]",
    "url": "sabaconst:https://www.linkedin.com/oauth/v2/accessToken",
    "grantType": "sabaconst:client_credentials"
  },
  "cachedHeaderProperty": "com.saba.linkedin.catalog.token"
}
```

### 2.2 Session Token (Kaltura)
- Used when the API requires generating a session token via a dedicated endpoint.
- Implemented as a custom `AbstractReusableAuthStrategy` inner class inside Flows.java.
- The strategy calls the vendor's session API, caches the token in message headers.

**Example (from Kaltura KalturaFlows.KalturaSessionAuthenticationStrategy):**
```java
public static class KalturaSessionAuthenticationStrategy extends AbstractReusableAuthStrategy {
    @Override
    public MultiValueMap<String, String> getAuthHeaders(Message message) {
        String baseUrl = resolve(message, "baseUrl");
        String partnerId = resolve(message, "partnerId");
        String secret = resolve(message, "adminSecret");
        String ks = generateKalturaSession(baseUrl, partnerId, secret, sessionType);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("KALTURA_SESSION_TOKEN", ks);
        headers.add("KALTURA_API_URL", finalUrl);
        return headers;
    }
}
```

### 2.3 Basic Auth (Udemy)
- Used when the API accepts Base64-encoded `clientId:clientSecret` in the Authorization header.
- Configured by calling `BasicAuthenticationStrategy.getAuthorizationToken(clientId, clientSecret)`.

### 2.4 API Key
- Simple header-based auth. Pass the key as a custom header (e.g., `X-API-Key`).
- Configured in the Content.js `headers` block.

---

## 3. FILE-BY-FILE DEEP DIVE

### 3.1 Constants.java

**Purpose:** Central registry of all string constants used across the connector.

**Pattern:**
```java
package com.saba.integration.apps.{connector};

public class {Connector}Constants {
    // Credential keys (must match account configuration keys in the UI)
    public static final String CLIENT_ID = "CLIENT_ID";
    public static final String CLIENT_SECRET = "CLIENT_SECRET";
    public static final String BASE_URL = "BASE_URL";

    // API endpoints
    public static final String TOKEN_URL = "https://api.vendor.com/oauth/token";
    public static final String CONTENT_URL = "https://api.vendor.com/v2/content";

    // Headers
    public static final String AUTH_HEADER = "Authorization";

    // Pagination
    public static final String PAGE_SIZE = "100";
    public static final String DEFAULT_PAGE = "1";

    // Session / token management
    public static final String SESSION_TOKEN_KEY = "SESSION_TOKEN";
}
```

**Rules:**
- One class, no methods, only `public static final String` fields.
- Keys like `CLIENT_ID`, `CLIENT_SECRET` must match what the admin UI sends as `accountConfigs`.
- API URLs should be stored as constants, NOT hardcoded elsewhere.

---

### 3.2 TestConnection.java

**Purpose:** Validates the user's credentials by making a test API call. Called when an admin clicks "Test Connection" in the Nova UI.

**Pattern:**
```java
package com.saba.integration.apps.{connector};

@Component
public class {Connector}TestConnection extends VendorTestConnection {

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        // 1. Extract credentials from accountConfigs
        String clientId = accountConfigs.get({Connector}Constants.CLIENT_ID).get(0);
        String clientSecret = accountConfigs.get({Connector}Constants.CLIENT_SECRET).get(0);

        // 2. Build the test request
        // 3. Call the vendor API
        // 4. Return success or failure response
        responses.add(testRestEndpoint(url, clientId, clientSecret));
        return responses;
    }

    @Override
    public String getType() {
        return VendorConstants.{CONNECTOR_UPPER};
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // Only needed for SOAP responses, can be empty for REST
    }
}
```

**Rules:**
- Must extend `VendorTestConnection`.
- Must be annotated with `@Component`.
- `getType()` must return the constant from `VendorConstants.java`.
- Extract all credentials from `accountConfigs` map.
- For OAuth2: POST to token URL with client_credentials grant.
- For Basic Auth: Build Authorization header and GET a test endpoint.
- For API Key: Add the key as a header and GET a test endpoint.

---

### 3.3 ComponentControl.java

**Purpose:** MINIMAL override of HTTPComponentControl. The base class handles 90% of the work.

> **CRITICAL RULE:** ComponentControl should be under 80 lines. The base class handles:
> - URL resolution from Content.js
> - Request body construction from Content.js multipartBody
> - Pagination via `#currentPageNumber` SpEL
> - Auth token injection via authenticate()
> - Response handling and loop termination

**Pattern (most connectors — no override needed):**
```java
package com.saba.integration.apps.{connector};

public class {Connector}ComponentControl extends HTTPComponentControl {

    @Override
    public HTTPComponentControl newInstance() {
        return new {Connector}ComponentControl();
    }

    // For most connectors, this is ALL you need.
    // Override nextRequest() ONLY if you need post-auth body manipulation.
}
```

**Pattern (Kaltura — auth token needs to move from header to body):**
```java
public class KalturaComponentControl extends HTTPComponentControl {
    @Override
    public HTTPComponentControl newInstance() {
        return new KalturaComponentControl();
    }

    @Override
    public HTTPRequestDTO nextRequest() {
        HTTPRequestDTO request = super.nextRequest();
        if (request != null) {
            // Move KS from headers to body (Kaltura-specific)
            promoteKsToBody(request);
        }
        return request;
    }
}
```

**DON'T:**
- ❌ Override onSuccess() for page counting — use `#currentPageNumber` in Content.js
- ❌ Implement custom retry logic — use Content.js `retryOptions`
- ❌ Manually manage pageIndex/pageNumber — the base class tracks this
- ❌ Add @Bean annotation — jsonType_registry.xml handles instantiation

---

### 3.4 Flows.java

**Purpose:** Spring @Configuration class that registers ONLY IntegrationFlow beans.

> **CRITICAL RULE:** NEVER create @Bean for ComponentControl or AuthenticationStrategy.
> The JSON type registry (`jsonType_registry.xml`) handles their instantiation.

**Pattern:**
```java
package com.saba.integration.apps.{connector};

@Configuration
public class {Connector}Flows {

    // Flow bean — one per entity type (content, learningpath, user, etc.)
    @Bean(name = "integration.{connector}.import.content")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow contentImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        return FlowGraphCreator.generateIntegrationFlow(
            "/com/saba/mapping/{connector}/flow/Content.js",
            "integration.{connector}.import.content",
            sourceNodeMap, id, true);
    }

    // For Session Token auth: static inner class extending AbstractReusableAuthStrategy
    // For OAuth2/BasicAuth: NO auth class needed — handled in Content.js
}
```

**Rules:**
- The flow path must match the actual JSON file location: `/com/saba/mapping/{connector}/flow/Content.js`
- Bean names must follow the convention: `integration.{connector}.import.{entity}`
- All beans must be `SCOPE_PROTOTYPE` (new instance per request)
- ❌ **NEVER** create @Bean for ComponentControl — registry handles it
- ❌ **NEVER** create @Bean for AuthenticationStrategy — registry handles it
- For OAuth2 (LinkedIn), the auth is in the Content.js — no custom strategy class needed
- For Session Token (Kaltura), use a static inner class extending `AbstractReusableAuthStrategy`

---

### 3.5 Content.js (Flow Definition)

**Purpose:** JSON pipeline definition that orchestrates the entire data fetch → transform → output process.

**Structure:**
```json
{
  "components": [
    // 1. HTTP component — fetches data from vendor API
    {
      "type": "http",
      "name": "{CONNECTOR}_Catalog_Http_Component",
      "control": {
        "type": "{connector}ComponentControl",
        "url": "sabaspel:payload.accountConfigs['BASE_URL'][0] + '/api/content?page=' + #currentPageNumber"
      },
      "requestMethod": "GET",
      "headers": { ... },
      "outputType": "FILE",
      "maxLoopCounter": "sabaspel:headers.headers['isPreview']==true ? '1' : '-1'",
      "responseValidator": { "type": "noMoreRecords", "recordQName": {...}, "json": true },
      "authenticationStrategy": { ... },
      "retryOptions": { "maxRetry": 3, "delaySeconds": 5 }
    },
    // 2. File path processor
    { "type": "filePathProcessor", ... },
    // 3. JSON to XML converter
    { "type": "jsonToXml", ... },
    // 4. XSLT transformer property
    { "type": "xsltTransformerProperty", ... },
    // 5. XSLT transformer — applies mapping
    { "type": "xslt", ... },
    // 6. File rename
    { "type": "fileNameRename", ... },
    // 7. Exception handler
    { "type": "exception", ... },
    // 8. Preview filter
    { "type": "previewFilter", ... },
    // 9. Monitoring components (success + failure for each step)
    { "type": "flowMonitoring", ... },
    // 10. Data fetch response
    { "type": "dataFetchResponseTransformer", ... },
    // 11. Router
    { "type": "router", ... }
  ],
  "flow": {
    "success": [
      // Main pipeline path
      ["SOURCE", "Http_Component", "HttpMonitoring", "FileProcessor", "JsonToXml", "JsonMonitoring", "XsltProperty", "Xslt", "XsltMonitoring", "CsvProcessor"],
      // No-records path
      ["CsvProcessor", "NoRecordsFilter", "NoRecordsMonitoring", "NoRecordsMeta", "Router"],
      // Has-records path
      ["CsvProcessor", "HasRecordsFilter", "FileRename", "EntityConfigUpdater", "MetaNode", "Router"],
      // Preview path
      ["JsonToXml", "PreviewFilter", "XmlMerger", "FileCopy"],
      // Error paths (one per step)
      ["HttpException", "Exception"],
      ["JsonException", "Exception"],
      ["XsltException", "Exception"],
      ["Exception", "Router"]
    ],
    "failure": [
      ["Http_Component", "HttpException"],
      ["JsonToXml", "JsonException"],
      ["Xslt", "XsltException"]
    ]
  },
  "name": "{Connector Name} Catalog Import",
  "integrationId": "integ{32-char-hex}",
  "entityId": "mpent{32-char-hex}"
}
```

**Key Concepts:**
- `sabaspel:` — Spring Expression Language references to runtime config values
- `payload.accountConfigs['KEY'][0]` — reads from admin UI configuration
- `headers.headers['KEY']` — reads from message headers (set by previous components)
- `#currentPageNumber` — built-in pagination variable
- `#loopCounter` — built-in loop counter
- `#toEpoch(date, format)` — date conversion helper

---

### 3.6 Mapping XML

**Purpose:** Maps vendor API response fields to EdCast internal fields.

**Pattern:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<integrationMapping>
  <mapping source="id" target="EXTERNAL_ID" required="true"/>
  <mapping source="title" target="TITLE" required="true"/>
  <mapping source="description" target="DESCRIPTION"/>
  <mapping source="url" target="LAUNCH_URL"/>
  <mapping source="imageUrl" target="IMAGE_URL"/>
  <mapping source="duration" target="DURATION"/>
  <mapping source="provider" target="PROVIDER_NAME" defaultValue="{ConnectorName}"/>
  <mapping source="type" target="CONTENT_TYPE" defaultValue="COURSE"/>
  <mapping source="language" target="LANGUAGE" defaultValue="en"/>
  <mapping source="lastModified" target="LAST_MODIFIED_DATE"/>

  <!-- Accept filter — controls which records are imported -->
  <acceptRecord>
    <filter field="status" value="ACTIVE"/>
  </acceptRecord>
</integrationMapping>
```

**EdCast Internal Fields (target values):**
| Field | Description | Required |
|---|---|---|
| EXTERNAL_ID | Unique ID from vendor | Yes |
| TITLE | Content title | Yes |
| DESCRIPTION | Content description | No |
| LAUNCH_URL | URL to launch the content | No |
| IMAGE_URL | Thumbnail image URL | No |
| DURATION | Duration in minutes | No |
| PROVIDER_NAME | Vendor name | No |
| CONTENT_TYPE | COURSE, VIDEO, ARTICLE, etc. | No |
| LANGUAGE | ISO language code | No |
| LAST_MODIFIED_DATE | Last update timestamp | No |

---

### 3.7 VendorConstants Patch

**Purpose:** Register the new connector's unique integration ID.

```java
// Add to VendorConstants.java
public static final String {CONNECTOR_UPPER} = "integ{32-char-hex}";
```

The hex ID must be globally unique. Generate a 32-character hex string.

---

### 3.8 DefaultMappingConfig Patch

**Purpose:** Register the mapping XML file so the framework can find it.

```java
// Add to DefaultMappingConfig.java enum
{CONNECTOR_UPPER}_CONTENT(
    VendorConstants.{CONNECTOR_UPPER},
    "mpent{32-char-hex}",
    "{connector}_edcast_content.xml"
),
```

---

### 3.9 DML Script

**Purpose:** Register the connector in the database.

```sql
-- 1. Register vendor entity
call mpp_vendor_entity_ins(
    'integ{32-char-hex}',         -- integration_id
    '{Connector}',                 -- display_name
    'Content',                     -- entity_type
    'IMPORT',                      -- direction
    'ACTIVE',                      -- status
    null                           -- optional params
);

-- 2. Register entity association
call mpp_integration_entity_assoc_ins(
    'minea{32-char-hex}',         -- assoc_id
    'integ{32-char-hex}',         -- integration_id
    'mpent{32-char-hex}',         -- entity_id
    'content',                     -- entity_type
    1,                             -- schedule_order
    'integration.{connector}.import.content'  -- flow_bean_name
);

-- 3. Register entity config
call mpp_entity_config_ins(
    'mpent{32-char-hex}',         -- entity_id
    'UPDATE_FROM',                 -- config_key
    '2020-01-01',                  -- default_value
    'DATE'                         -- value_type
);
```

---

## 4. DIRECTORY STRUCTURE

```
integration/apps/src/main/java/com/saba/integration/apps/{connector}/
├── {Connector}Constants.java
├── {Connector}TestConnection.java
├── {Connector}ComponentControl.java
└── {Connector}Flows.java

integration/apps/src/main/resources/com/saba/mapping/{connector}/
├── flow/
│   └── Content.js
└── default_data/
    └── {connector}_edcast_content.xml

marketplace/src/main/java/com/saba/integration/marketplace/
├── vendor/VendorConstants.java          (patch)
└── mapping/util/DefaultMappingConfig.java (patch)

database/
└── dml_patch.sql
```

---

## 5. ID FORMAT CONVENTIONS

| ID Prefix | Usage | Example |
|---|---|---|
| `integ` | Integration/vendor ID | `integ1575939913732429ee6e60b53a046cc0` |
| `mpent` | Marketplace entity ID | `mpent157852367760824ad188f04b3804c9b0` |
| `minea` | Integration-entity association ID | `minea1578523677123456789abcdef01234567` |

All IDs: prefix + 32 lowercase hex characters = 37 characters total.

---

## 6. SABASPEL EXPRESSION REFERENCE

| Expression | Meaning |
|---|---|
| `sabaspel:payload.accountConfigs['KEY'][0]` | Read config value from admin UI |
| `sabaspel:headers.headers['KEY']` | Read from message header |
| `sabaspel:payload.entityConfigs['KEY'][0]` | Read entity-level config |
| `sabaspel:integrationMeta.tenant` | Current tenant ID |
| `sabaspel:#currentPageNumber` | Current pagination page |
| `sabaspel:#loopCounter` | Current loop iteration |
| `sabaspel:#toEpoch(date, format)` | Convert date to epoch |
| `sabaconst:value` | Static constant value |

---

## 7. HOW TO BUILD A NEW CONNECTOR (STEP-BY-STEP)

1. **Read the vendor API docs** — Identify: auth method, content list endpoint, pagination style, response schema.
2. **Create Constants.java** — Define all config keys and API URLs.
3. **Create TestConnection.java** — Implement credential validation.
4. **Create ComponentControl.java** — Handle headers, pagination, request body.
5. **Create Flows.java** — Define flow beans and auth strategy.
6. **Create Content.js** — Build the data pipeline (HTTP → jsonToXml → XSLT).
7. **Create mapping XML** — Map API response fields to EdCast fields.
8. **Patch VendorConstants** — Add integration ID.
9. **Patch DefaultMappingConfig** — Register mapping file.
10. **Create DML script** — Register in database.

---

## 8. COMMON MISTAKES TO AVOID

1. **@Bean for ComponentControl/AuthStrategy** — NEVER. The JSON type registry handles instantiation.
2. **Fat ComponentControl** — Should be under 80 lines. Base class does the work.
3. **Manual pagination in Java** — Use `#currentPageNumber` SpEL in Content.js.
4. **Duplicated auth logic** — Auth code in ONE place. TestConnection delegates to Flows.
5. **Mismatched IDs** — The `integrationId` in Content.js must match VendorConstants and DML.
6. **Wrong bean names** — The flow bean name in Flows.java must match the DML `flow_bean_name`.
7. **Missing accountConfigs keys** — Constants must match what the admin UI sends.
8. **Hardcoded URLs** — Use `sabaspel:payload.accountConfigs['BASE_URL'][0]` not hardcoded strings.
9. **No error handling** — Every step needs success AND failure monitoring components.
10. **Wrong scope** — All beans must be `SCOPE_PROTOTYPE`.
11. **No preview controls** — Always: `maxLoopCounter '1' + pageSize '10'` for preview.

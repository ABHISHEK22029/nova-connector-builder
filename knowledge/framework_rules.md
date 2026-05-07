# Nova Connector Framework — Non-Negotiable Rules

These rules are derived from production refactoring and code review. They MUST be followed for every connector.

---

## RULE 1: No @Bean for ComponentControl or AuthenticationStrategy

The JSON type registry (`jsonType_registry.xml`) handles instantiation of ComponentControl and AuthenticationStrategy classes. Only define `@Bean` for `IntegrationFlow` objects.

**WRONG:**
```java
@Bean(name = "kalturaComponentControl")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public KalturaComponentControl componentControl() {
    return new KalturaComponentControl();
}
```

**RIGHT:**
```java
// NO @Bean for ComponentControl — registry handles it
// Only IntegrationFlow beans:
@Bean(name = "integration.kaltura.import.content")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public IntegrationFlow kalturaContentImportFlow(...) { ... }
```

---

## RULE 2: Use #currentPageNumber, NOT Manual Page Tracking

The base `HTTPComponentControl` tracks pages automatically. Use `#currentPageNumber` SpEL expression in Content.js.

**WRONG (in ComponentControl.java):**
```java
private int currentPageIndex = 1;

@Override
public void onSuccess(Message message) {
    currentPageIndex++;
}
```

**RIGHT (in Content.js):**
```json
"pager[pageIndex]": ["sabaspel:T(String).valueOf(#currentPageNumber)"]
```

---

## RULE 3: All Strings Must Be Constants

Every hardcoded string in ComponentControl, Flows, or TestConnection must be a constant in `{Connector}Constants.java`.

**WRONG:**
```java
String token = headers.get("KALTURA_SESSION_TOKEN");
log.info("[KalturaControl] Token found");
```

**RIGHT:**
```java
String token = headers.get(KalturaConstants.HEADER_SESSION_TOKEN);
log.info(KalturaConstants.LOG_PREFIX + "Token found");
```

---

## RULE 4: Auth Caching via AbstractReusableAuthStrategy

The `cachedHeaderProperty` in Content.js handles token caching automatically. `getAuthHeaders()` is called ONCE per flow run. On 401, the cache is cleared and it retries.

**WRONG:**
```java
// Manual token validation in ComponentControl
if (tokenExpired()) {
    refreshToken();
}
```

**RIGHT:**
```json
// In Content.js — framework handles caching and retry:
"authenticationStrategy": {
    "type": "kalturaSession",
    "detail": { ... },
    "cachedHeaderProperty": "com.saba.kaltura.media.token"
}
```

---

## RULE 5: ComponentControl Should Be Minimal

Base class `HTTPComponentControl` handles: URL resolution, body building, pagination, authentication.
Override `nextRequest()` ONLY if you need post-authentication body manipulation (like moving a token from headers to body).

**WRONG:** 200+ line ComponentControl with custom pagination, retry, header injection
**RIGHT:** 40-80 line ComponentControl with one override if needed, or empty if not

---

## RULE 6: TestConnection Delegates to Shared Auth

If `Flows.java` has an auth method (e.g., `generateKalturaSession`), TestConnection MUST call it.
Never duplicate auth logic between TestConnection and the AuthStrategy.

**WRONG:**
```java
// In TestConnection — duplicated auth logic
private String generateToken(String baseUrl, String secret) {
    // 50 lines of duplicated HTTP call code...
}
```

**RIGHT:**
```java
// In TestConnection — delegates to shared method
String ks = KalturaFlows.KalturaSessionAuthenticationStrategy
        .generateKalturaSession(baseUrl, partnerId, secret, sessionType);
```

---

## RULE 7: Preview = maxLoopCounter '1' + pageSize '10'

Preview (sample data) ALWAYS uses exactly these settings. No exceptions.

```json
"maxLoopCounter": "sabaspel:headers.headers['isPreview']==true ? '1' : '-1'",
"pager[pageSize]": ["sabaspel:headers.headers['isPreview']==true ? '10' : '100'"]
```

This means: preview fetches exactly 1 page of 10 records = 10 records max.

---

## RULE 8: Bean Name Convention

Flow bean names MUST follow: `integration.{connector}.import.{entity}`

Examples:
- `integration.kaltura.import.content`
- `integration.udemy.import.content`
- `integration.udemy.import.learningPath`

This name is referenced in:
1. `Flows.java` — `@Bean(name = "...")`
2. DML script — `mpp_integration_entity_assoc_ins` flow_bean_name parameter
3. Content.js — implicitly via the flow path

---

## RULE 9: Content.js Must Have Complete Error Handling

Every processing step needs both success AND failure monitoring components in the flow routing:

```json
"flow": {
    "success": [
        ["SOURCE", "Http_Component", "HttpMonitoring", ...],
        ["HttpException", "Exception"]  // Error path
    ],
    "failure": [
        ["Http_Component", "HttpException"]  // Failure trigger
    ]
}
```

---

## RULE 10: Content.js Auth Patterns by Type

### Session Token (Kaltura):
```json
"authenticationStrategy": {
    "type": "kalturaSession",  // Maps to class in jsonType_registry.xml
    "detail": {
        "partnerId": "sabaspel:...",
        "adminSecret": "sabaspel:...",
        "baseUrl": "sabaspel:..."
    },
    "cachedHeaderProperty": "com.saba.kaltura.media.token"
}
```

### OAuth2 Client Credentials (LinkedIn, Udemy):
```json
"authenticationStrategy": {
    "type": "oauth2v2",
    "detail": {
        "clientId": "sabaspel:payload.accountConfigs['CLIENT_ID'][0]",
        "clientSecret": "sabaspel:payload.accountConfigs['CLIENT_SECRET'][0]",
        "url": "sabaconst:https://api.vendor.com/oauth/token",
        "grantType": "sabaconst:client_credentials"
    },
    "cachedHeaderProperty": "com.saba.vendor.catalog.token"
}
```

### Basic Auth (Udemy):
- No authenticationStrategy in Content.js
- ComponentControl builds the Authorization header:
```java
String encoded = Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
httpRequestDTO.getHeaders().add("Authorization", "Basic " + encoded);
```

---

## RULE 11: sabaspel Expression Patterns

| Expression | Usage | Example |
|---|---|---|
| `sabaspel:payload.accountConfigs['KEY'][0]` | Read admin UI config | `sabaspel:payload.accountConfigs['SERVICE_URL'][0]` |
| `sabaspel:headers.headers['KEY']` | Read message header | `sabaspel:headers.headers['isPreview']` |
| `sabaspel:headers.headers['integrationDataFetchDetail'].accountConfigs['KEY'][0]` | Read config inside flow | Most common pattern |
| `sabaspel:payload.entityConfigs['KEY'][0]` | Read entity config | `sabaspel:payload.entityConfigs['UPDATE_FROM'][0]` |
| `sabaspel:#currentPageNumber` | Current page (1-based) | Pagination |
| `sabaspel:#loopCounter` | Current loop iteration (0-based) | Header inclusion logic |
| `sabaspel:#sysDateInISO()` | Current timestamp in ISO | Sync timestamps |
| `sabaspel:#getConfValueOrDefault(configs,'KEY','default')` | Config with default | Update dates |
| `sabaconst:value` | Static constant | URLs, grant types |

---

## RULE 12: Flow Pipeline Component Order

Every content import connector follows this pipeline:

```
SOURCE → ResolvedProperties → MetaDataSetter → DateMonitoring
→ HTTP_Component → HttpMonitoring → FileProcessor
→ JsonToXml → JsonMonitoring → XsltProperty → Xslt → XsltMonitoring
→ CsvRecordExistsProcessor → [HasRecords/NoRecords branching]
→ EntityConfigUpdater → MetaNode → Router
```

Preview branch (always present):
```
JsonToXml → PreviewFilter → XmlMerger → FileCopy
```

Error branches (one per step):
```
Http_Component → HttpException → Exception → Router
JsonToXml → JsonException → Exception → Router
Xslt → XsltException → Exception → Router
```

---

## RULE 13: API Docs Upload Format

When users upload API documentation for a new connector, accept these formats:
- **JSON** (OpenAPI/Swagger spec preferred)
- **YAML** (OpenAPI/Swagger spec)
- **Markdown** (structured docs with endpoints, auth, response schemas)

The uploaded docs must contain:
1. Authentication method and endpoints
2. Content listing endpoint(s)
3. Response schema with field descriptions
4. Pagination method (offset, cursor, page-based)

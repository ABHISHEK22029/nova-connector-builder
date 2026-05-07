# Authentication Patterns Reference — Nova Connectors

## PURPOSE
This document catalogs every authentication pattern used in Nova connectors. An AI model generating a new connector MUST select and implement one of these patterns based on the target API's requirements.

---

## PATTERN 1: OAuth2 Client Credentials

**When to use:** API requires `client_id` + `client_secret` exchanged for an access token.
**Connectors using this:** LinkedIn Learning, Cornerstone (CSX)

### Implementation

**No custom Java class needed.** Auth is configured entirely in Content.js:

```json
{
  "authenticationStrategy": {
    "type": "oauth2v2",
    "detail": {
      "invalidateAfterSeconds": 0,
      "retryCount": 0,
      "sendClientCredentialsInBody": "true",
      "clientId": "sabaspel:payload.accountConfigs['CLIENT_ID'][0]",
      "clientSecret": "sabaspel:payload.accountConfigs['CLIENT_SECRET'][0]",
      "url": "sabaconst:https://api.vendor.com/oauth/token",
      "grantType": "sabaconst:client_credentials"
    },
    "cachedHeaderProperty": "com.saba.{connector}.catalog.token"
  }
}
```

**TestConnection.java for OAuth2:**
```java
@Override
public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
    List<TestConnectionResponse> responses = new ArrayList<>();
    String clientId = accountConfigs.get(CLIENT_ID).get(0);
    String clientSecret = accountConfigs.get(CLIENT_SECRET).get(0);
    
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "client_credentials");
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    
    responses.add(testRestEndpoint(body));
    return responses;
}
```

### Required Constants
```java
public static final String CLIENT_ID = "CLIENT_ID";
public static final String CLIENT_SECRET = "CLIENT_SECRET";
public static final String TOKEN_URL = "https://api.vendor.com/oauth/token";
public static final String GRANT_TYPE = "client_credentials";
```

---

## PATTERN 2: Session Token / Custom Auth

**When to use:** API requires a custom session generation call (e.g., Kaltura KS token).
**Connectors using this:** Kaltura

### Implementation

**Requires a custom inner class in Flows.java:**

```java
@Configuration
public class {Connector}Flows {

    @Bean(name = "{connector}Session")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public {Connector}AuthStrategy authStrategy() {
        return new {Connector}AuthStrategy();
    }

    public static class {Connector}AuthStrategy extends AbstractReusableAuthStrategy {
        private Map<String, Object> detail;
        
        public void setDetail(Map<String, Object> detail) { this.detail = detail; }
        public Map<String, Object> getDetail() { return detail; }

        @Override
        public void authenticate(Message message, HTTPRequestDTO request) {
            super.authenticate(message, request);
            // Promote cached tokens from header cache to message headers
            String cachedProp = getCachedHeaderProperty(message);
            if (cachedProp != null) {
                Object cached = message.getHeaders().get(cachedProp);
                if (cached instanceof MultiValueMap) {
                    MultiValueMap<String, String> tokens = (MultiValueMap) cached;
                    String token = tokens.getFirst("SESSION_TOKEN");
                    if (token != null) {
                        message.getHeaders().put("SESSION_TOKEN", token);
                    }
                }
            }
        }

        @Override
        public MultiValueMap<String, String> getAuthHeaders(Message message) {
            String baseUrl = resolve(message, "baseUrl");
            String apiKey = resolve(message, "apiKey");
            
            // Call vendor session API
            String token = callSessionApi(baseUrl, apiKey);
            
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add("SESSION_TOKEN", token);
            headers.add("API_URL", baseUrl + "/api/content");
            return headers;
        }
        
        private String resolve(Message message, String key) {
            Object val = detail.get(key);
            return val == null ? null : (String) ResolverUtil.resolveValue(message, (String) val, null);
        }
    }
}
```

**Content.js auth block for custom strategy:**
```json
{
  "authenticationStrategy": {
    "type": "{connector}Session",
    "detail": {
      "baseUrl": "sabaspel:payload.accountConfigs['BASE_URL'][0]",
      "apiKey": "sabaspel:payload.accountConfigs['API_KEY'][0]"
    },
    "cachedHeaderProperty": "com.saba.{connector}.session.token"
  }
}
```

---

## PATTERN 3: Basic Authentication

**When to use:** API accepts `Authorization: Basic base64(clientId:clientSecret)`.
**Connectors using this:** Udemy

### Implementation

**No custom auth strategy needed.** Use the built-in `BasicAuthenticationStrategy`:

**TestConnection.java:**
```java
private HttpHeaders getHeaders(String clientId, String clientSecret) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION,
        BasicAuthenticationStrategy.getAuthorizationToken(clientId, clientSecret));
    return headers;
}

private TestConnectionResponse testRestEndpoint(String endpoint, String clientId, String clientSecret) {
    HttpHeaders headers = getHeaders(clientId, clientSecret);
    HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(null, headers);
    return getApiResponse(String.class, HttpMethod.GET, endpoint, entity, null, false);
}
```

**Content.js auth block:**
```json
{
  "authenticationStrategy": {
    "type": "basicAuth",
    "detail": {
      "username": "sabaspel:payload.accountConfigs['CLIENT_ID'][0]",
      "password": "sabaspel:payload.accountConfigs['CLIENT_SECRET'][0]"
    }
  }
}
```

---

## PATTERN 4: API Key Header

**When to use:** API requires a custom header like `X-API-Key: <key>`.
**Connectors using this:** Custom vendors

### Implementation

**Content.js — add key to headers:**
```json
{
  "headers": {
    "X-API-Key": ["sabaspel:payload.accountConfigs['API_KEY'][0]"]
  }
}
```

**ComponentControl.java — inject at request time:**
```java
@Override
public void getHeaders(Message message, HTTPRequestDTO request) {
    String apiKey = (String) message.getHeaders().get("API_KEY");
    if (apiKey != null) {
        request.getHeaders().add("X-API-Key", apiKey);
    }
}
```

---

## PATTERN 5: Bearer Token (Pre-configured)

**When to use:** The admin provides a static bearer token.

```json
{
  "headers": {
    "Authorization": ["sabaspel:'Bearer ' + payload.accountConfigs['BEARER_TOKEN'][0]"]
  }
}
```

---

## DECISION MATRIX

| If the API uses... | Use Pattern | Custom Java Class? |
|---|---|---|
| OAuth2 client_credentials | Pattern 1 (oauth2v2) | No |
| Custom session/token endpoint | Pattern 2 (AbstractReusableAuthStrategy) | Yes (inner class in Flows.java) |
| Basic username:password | Pattern 3 (basicAuth) | No |
| API key in header | Pattern 4 (header injection) | No |
| Static bearer token | Pattern 5 (header) | No |

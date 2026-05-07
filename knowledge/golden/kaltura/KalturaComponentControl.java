/*
 * ============================================================================
 * GOLDEN REFERENCE: KalturaComponentControl.java
 * ============================================================================
 * AUTH TYPE: Session Token (KS moved from header to POST body)
 * 
 * CRITICAL PATTERN RULES:
 * 1. ComponentControl should be MINIMAL — the base class handles 90% of work
 * 2. Base class HTTPComponentControl provides:
 *    - URL resolution from Content.js
 *    - Request body construction from Content.js multipartBody
 *    - Pagination via #currentPageNumber SpEL
 *    - Auth token injection via authenticate()
 *    - Response handling and loop termination
 * 3. Override nextRequest() ONLY if you need post-auth manipulation
 * 4. NEVER override onSuccess() for page counting — base class tracks pages
 * 5. NEVER implement custom retry logic — Content.js retryOptions handles it
 * 6. NEVER manually manage currentPageIndex — use #currentPageNumber in Content.js
 * 7. This class is instantiated by jsonType_registry.xml, NOT by @Bean
 * 8. The "type": "kalturaComponentControl" in Content.js maps to this class
 * 
 * FOR MOST CONNECTORS:
 *   - If auth goes in headers (OAuth2, Basic Auth, API Key): NO override needed
 *   - If auth goes in body (Kaltura KS): Override nextRequest() to move it
 *   - If pagination is URL-based: Handle in Content.js URL SpEL expression
 * ============================================================================
 */
package com.saba.integration.apps.kaltura;

import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.http.HTTPComponentControl;
import com.saba.integration.http.HTTPRequestDTO;
import com.saba.kernel.logging.SabaLogger;
import org.springframework.util.MultiValueMap;

public class KalturaComponentControl extends HTTPComponentControl {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.KalturaLogger);

    @Override
    public HTTPComponentControl newInstance() {
        return new KalturaComponentControl();
    }

    /*
     * nextRequest() is called AFTER authenticate().
     * At this point, auth headers are already populated by the framework.
     * We call super.nextRequest() first to get the fully-constructed request,
     * then modify it (move KS from header to body).
     */
    @Override
    public HTTPRequestDTO nextRequest() {
        HTTPRequestDTO request = super.nextRequest();
        if (request != null) {
            promoteKsToBody(request);
        }
        return request;
    }

    /*
     * Kaltura-specific: move KS token from HTTP headers to POST body.
     * Most connectors DON'T need this — only when the API requires
     * auth in the body rather than headers.
     */
    @SuppressWarnings("unchecked")
    private void promoteKsToBody(HTTPRequestDTO request) {
        String ks = request.getHeaders() != null
                ? request.getHeaders().getFirst(KalturaConstants.HEADER_SESSION_TOKEN)
                : null;

        if (ks == null) {
            log.warn(KalturaConstants.LOG_PREFIX + "KS token not found in request headers — request may fail.");
            return;
        }

        log.debug(KalturaConstants.LOG_PREFIX + "Promoting KS token to POST body. Length: " + ks.length());

        if (request.getBody() instanceof MultiValueMap) {
            MultiValueMap<String, Object> body = (MultiValueMap<String, Object>) request.getBody();
            body.remove(KalturaConstants.PARAM_KS);
            body.add(KalturaConstants.PARAM_KS, ks);
        } else if (request.getBody() != null) {
            String bodyStr = request.getBody().toString();
            if (!bodyStr.contains(KalturaConstants.PARAM_KS + "=")) {
                String separator = bodyStr.isEmpty() ? "" : "&";
                request.setBody(bodyStr + separator + KalturaConstants.PARAM_KS + "=" + ks);
            }
        }

        // Remove internal headers from outgoing HTTP request
        request.getHeaders().remove(KalturaConstants.HEADER_SESSION_TOKEN);
        request.getHeaders().remove(KalturaConstants.HEADER_API_URL);
    }
}

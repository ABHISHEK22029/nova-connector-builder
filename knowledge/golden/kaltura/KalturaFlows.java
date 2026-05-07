/*
 * ============================================================================
 * GOLDEN REFERENCE: KalturaFlows.java
 * ============================================================================
 * AUTH TYPE: Session Token (KS)
 * 
 * CRITICAL PATTERN RULES:
 * 1. ONLY @Bean for IntegrationFlow — NEVER for ComponentControl or AuthStrategy
 *    The JSON type registry (jsonType_registry.xml) instantiates those automatically
 * 2. Auth strategy is a STATIC INNER CLASS extending AbstractReusableAuthStrategy
 * 3. Only override getAuthHeaders() — never authenticate()
 * 4. The static generateKalturaSession method is package-private so TestConnection 
 *    can reuse it (DRY principle — auth logic in ONE place)
 * 5. Bean name format: "integration.{connector}.import.{entity}"
 * 6. All beans must be SCOPE_PROTOTYPE (new instance per flow execution)
 * 7. Flow path must match actual resource: /com/saba/mapping/{connector}/flow/Content.js
 * 
 * FOR OAUTH2 CONNECTORS (LinkedIn, Udemy):
 *   - No inner class needed — auth is defined in Content.js authenticationStrategy block
 *   - Flows.java only has flow beans
 * ============================================================================
 */
package com.saba.integration.apps.kaltura;

import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.apps.util.FlowGraphCreator;
import com.saba.integration.component.IntegrationSource;
import com.saba.integration.http.authentication.AbstractReusableAuthStrategy;
import com.saba.integration.message.Message;
import com.saba.integration.resolver.ResolverUtil;
import com.saba.integration.stream.IntegrationStreamNode;
import com.saba.integration.stream.flow.IntegrationFlow;
import com.saba.kernel.logging.SabaLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

/*
 * RULE: @Configuration marks this as a Spring config class.
 * Only IntegrationFlow @Bean definitions go here.
 * NO @Bean for ComponentControl or AuthStrategy.
 */
@Configuration
public class KalturaFlows {
    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.KalturaLogger);

    /*
     * Flow Bean: One per entity type (content, user, learningPath, etc.)
     * - Bean name MUST match the flow_bean_name in the DML registration
     * - Path MUST match the actual Content.js resource location
     * - SCOPE_PROTOTYPE = new instance per flow execution
     */
    @Bean(name = "integration.kaltura.import.content")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow kalturaContentImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        log.info("#KalturaFlows: Creating content import flow for id=" + id);
        try {
            IntegrationFlow flow = FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/kaltura/flow/Content.js",
                    "integration.kaltura.import.content",
                    sourceNodeMap,
                    id,
                    true);
            if (flow == null) {
                log.error("#KalturaFlows: FlowGraphCreator returned null for id=" + id);
            }
            return flow;
        } catch (Throwable e) {
            log.error("#KalturaFlows: Failed to create flow for id=" + id, e);
            return null;
        }
    }

    /*
     * AUTH STRATEGY (Session Token Pattern):
     * 
     * WHY static inner class:
     * - It's instantiated by the JSON type registry, not by @Bean
     * - Content.js references it as: "type": "kalturaSession"
     * - The registry maps "kalturaSession" → this class
     * 
     * WHY extends AbstractReusableAuthStrategy:
     * - Handles caching automatically (cachedHeaderProperty in Content.js)
     * - Handles 401 retry automatically (clears cache, calls getAuthHeaders again)
     * - Only getAuthHeaders() needs to be implemented
     * 
     * WHY package-private static generateKalturaSession():
     * - TestConnection needs to call the same auth logic
     * - DRY: auth code exists in ONE place only
     */
    public static class KalturaSessionAuthenticationStrategy extends AbstractReusableAuthStrategy {

        private Map<String, Object> detail;

        public void setDetail(Map<String, Object> detail) {
            this.detail = detail;
        }

        public Map<String, Object> getDetail() {
            return detail;
        }

        /*
         * getAuthHeaders() is called by the framework ONCE per flow run.
         * The returned headers are cached under cachedHeaderProperty.
         * On 401, the cache is cleared and this is called again.
         * 
         * Returns: MultiValueMap with auth tokens as message headers
         * These headers are then accessible in ComponentControl and Content.js
         */
        @Override
        public MultiValueMap<String, String> getAuthHeaders(Message message) {
            log.info(KalturaConstants.LOG_AUTH_PREFIX + "Generating authentication headers");

            if (detail == null) {
                log.error(KalturaConstants.LOG_AUTH_PREFIX + "Config details missing");
                return new LinkedMultiValueMap<>();
            }

            // resolve() reads values from the "detail" map in Content.js authenticationStrategy
            // sabaspel: expressions are resolved at runtime against the current message
            String baseUrl = resolve(message, "baseUrl");
            String partnerId = resolve(message, "partnerId");
            String secret = resolve(message, "adminSecret");
            String sessionTypeStr = resolve(message, "sessionType");

            if (StringUtils.isEmpty(baseUrl) || StringUtils.isEmpty(partnerId) || StringUtils.isEmpty(secret)) {
                log.error(KalturaConstants.LOG_AUTH_PREFIX + "Missing required config");
                throw new RuntimeException("Missing Kaltura Configuration");
            }

            int sessionType = KalturaConstants.SESSION_TYPE_ADMIN;
            if (StringUtils.isNotEmpty(sessionTypeStr)) {
                try {
                    sessionType = Integer.parseInt(sessionTypeStr);
                } catch (NumberFormatException e) {
                    log.warn(KalturaConstants.LOG_AUTH_PREFIX + "Invalid SessionType, using default: "
                            + KalturaConstants.SESSION_TYPE_ADMIN);
                }
            }

            String ks = generateKalturaSession(baseUrl, partnerId, secret, sessionType);

            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            if (StringUtils.isNotEmpty(ks)) {
                headers.add(KalturaConstants.HEADER_SESSION_TOKEN, ks);
                log.info(KalturaConstants.LOG_AUTH_PREFIX + "KS token generated successfully");

                String finalUrl = baseUrl;
                if (message.getHeaders().get(KalturaConstants.HEADER_API_PATH) != null) {
                    String apiPath = (String) message.getHeaders().get(KalturaConstants.HEADER_API_PATH);
                    if (StringUtils.isNotEmpty(apiPath)) {
                        finalUrl = baseUrl + apiPath;
                    }
                }
                headers.add(KalturaConstants.HEADER_API_URL, finalUrl);
            } else {
                throw new RuntimeException(KalturaConstants.ERROR_SESSION_FAILED);
            }
            return headers;
        }

        private String resolve(Message message, String key) {
            Object val = detail.get(key);
            if (val == null) return null;
            return (String) ResolverUtil.resolveValue(message, (String) val, null);
        }

        /*
         * Package-private static so KalturaTestConnection can call it.
         * This is the SINGLE source of truth for KS token generation.
         */
        static String generateKalturaSession(String baseUrl, String partnerId, String secret, int sessionType) {
            try {
                String sessionUrl = baseUrl + KalturaConstants.SESSION_URL_PATH;
                StringBuilder body = new StringBuilder();
                body.append("secret=").append(secret)
                        .append("&userId=")
                        .append("&type=").append(sessionType)
                        .append("&partnerId=").append(partnerId)
                        .append("&expiry=").append(KalturaConstants.DEFAULT_SESSION_EXPIRY)
                        .append("&format=").append(KalturaConstants.JSON_FORMAT);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> response = restTemplate.exchange(sessionUrl, HttpMethod.POST, entity,
                        String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    String responseBody = response.getBody();
                    if (StringUtils.isNotEmpty(responseBody)) {
                        if (responseBody.trim().startsWith("{")) {
                            log.error(KalturaConstants.LOG_AUTH_PREFIX
                                    + "Session generation failed with API error: " + responseBody);
                            return null;
                        }
                        if (responseBody.contains("<result>") && responseBody.contains("</result>")) {
                            int start = responseBody.indexOf("<result>") + 8;
                            int end = responseBody.indexOf("</result>");
                            if (start < end) {
                                return responseBody.substring(start, end).trim();
                            }
                        }
                        return responseBody.replaceAll("^\"|\"$", "");
                    }
                }
                return null;
            } catch (Exception e) {
                log.error(KalturaConstants.LOG_AUTH_PREFIX + "Error calling session API", e);
                return null;
            }
        }
    }
}

/*
 * ============================================================================
 * GOLDEN REFERENCE: UdemyTestConnection.java — Basic Auth Pattern
 * ============================================================================
 * AUTH TYPE: Basic Auth (client_id:client_secret → Base64 header)
 * 
 * PATTERN HIGHLIGHTS:
 * 1. Uses BasicAuthenticationStrategy.getAuthorizationToken() for Base64 encoding
 * 2. Tests by calling a real API endpoint (page_size=1 for minimal data)
 * 3. Uses framework's getApiResponse() helper — no manual RestTemplate
 * 4. Simple and clean — under 60 lines
 * 
 * NOTE: No EdCast source test here (older connector).
 * New connectors SHOULD add edcastTestConnectionUtil.testEdcast(sourceId).
 * ============================================================================
 */
package com.saba.integration.apps.udemy;

import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.http.authentication.basic.BasicAuthenticationStrategy;
import com.saba.integration.marketplace.account.test.TestConnectionResponse;
import com.saba.integration.marketplace.vendor.VendorConstants;
import com.saba.integration.marketplace.account.test.VendorTestConnection;
import com.saba.kernel.logging.SabaLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UdemyTestConnection extends VendorTestConnection {
    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdemyLogger);

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        if (accountConfigs != null) {
            // Extract credentials using constants — never hardcode key strings
            String clientId = accountConfigs.get(UdemyConstants.CLIENT_ID) != null
                    ? accountConfigs.get(UdemyConstants.CLIENT_ID).get(0) : null;
            String clientSecret = accountConfigs.get(UdemyConstants.CLIENT_SECRET) != null
                    ? accountConfigs.get(UdemyConstants.CLIENT_SECRET).get(0) : null;
            String portalId = accountConfigs.get(UdemyConstants.PORTALID) != null
                    ? accountConfigs.get(UdemyConstants.PORTALID).get(0) : null;
            String portalUrl = accountConfigs.get(UdemyConstants.PORTALURL) != null
                    ? accountConfigs.get(UdemyConstants.PORTALURL).get(0) : null;

            // Build test URL with page_size=1 to minimize data transfer
            String url = portalUrl + "/api-2.0/organizations/" + portalId + "/course-collection-sync?page_size=1";
            responses.add(testRestEndpoint(url, clientId, clientSecret));
        }
        return responses;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // Empty for REST — only needed for SOAP
    }

    @Override
    public String getType() {
        return VendorConstants.UDEMY;
    }

    /*
     * Basic Auth: Base64-encode "clientId:clientSecret" and add as Authorization header
     * Uses framework's BasicAuthenticationStrategy helper
     */
    private HttpHeaders getHeaders(String clientId, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION,
                BasicAuthenticationStrategy.getAuthorizationToken(clientId, clientSecret));
        return headers;
    }

    private TestConnectionResponse testRestEndpoint(String endpoint, String clientId, String clientSecret) {
        HttpHeaders headers = getHeaders(clientId, clientSecret);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(null, headers);
        // Use framework helper — handles response parsing and status
        return getApiResponse(String.class, HttpMethod.GET, endpoint, requestEntity, null, false);
    }
}

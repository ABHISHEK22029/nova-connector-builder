package com.saba.integration.apps.zoom;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.saba.integration.apps.zoom.util.ZoomOAuthClient;
import com.saba.integration.core.model.TestConnectionResponse;
import com.saba.integration.core.model.TestConnectionStatus;
import com.saba.integration.core.service.AbstractTestConnection;
import com.saba.integration.core.service.impl.ServiceLocator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZoomTestConnection extends AbstractTestConnection {

    @Override
    public TestConnectionResponse testConnection(Map<String, List<String>> configs) {
        String clientId = getConfigValue(configs, ZoomConstants.CLIENT_ID);
        String clientSecret = getConfigValue(configs, ZoomConstants.CLIENT_SECRET);
        String accountId = getConfigValue(configs, ZoomConstants.ZOOM_ACCOUNT_ID);

        return testZoomAPI(clientId, clientSecret, accountId);
    }

    private TestConnectionResponse testZoomAPI(String clientId, String clientSecret, String accountId) {
        try {
            log.info("Testing connection to Zoom with clientId=" + clientId + " and accountId=" + accountId);

            // Build request body for token exchange
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "account_credentials");
            map.add("account_id", accountId);

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(clientId, clientSecret);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            // Make the API call to get the access token
            ZoomOAuthClient restTemplate = new ZoomOAuthClient();
            TestConnectionResponse response = restTemplate.getApiResponse(String.class, HttpMethod.POST, ZoomConstants.AUTHENTICATION_URL, request, null, false);

            if (response.getStatus() == TestConnectionStatus.SUCCESS) {
                log.info("Successfully obtained Zoom access token - Authentication valid");
                response.setResponse("Zoom authentication successful");
            } else {
                log.error("Failed to obtain Zoom access token. Invalid credentials.");
                response.setResponse("Failed to obtain Zoom access token. Invalid credentials.");
            }

            return response;

        } catch (Exception e) {
            log.error("Test connection failed", e);
            TestConnectionResponse response = new TestConnectionResponse();
            response.setStatus(TestConnectionStatus.FAILURE);
            response.setResponse("Test connection failed: " + e.getMessage());
            return response;
        }
    }

    /**
     * Get configuration value from map
     */
    private String getConfigValue(Map<String, List<String>> configs, String key) {
        List<String> values = configs.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // No specific validation needed for Zoom OAuth response
    }
}
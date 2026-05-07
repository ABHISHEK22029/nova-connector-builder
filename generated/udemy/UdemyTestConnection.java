package com.saba.integration.apps.udemy;

import com.saba.integration.apps.edcast.EdcastTestConnectionUtil;
import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.edcast.EdCastConstants;
import com.saba.integration.http.authentication.basic.BasicAuthenticationStrategy;
import com.saba.integration.marketplace.account.test.TestConnectionResponse;
import com.saba.integration.marketplace.account.test.TestConnectionStatus;
import com.saba.integration.marketplace.vendor.VendorConstants;
import com.saba.integration.marketplace.account.test.VendorTestConnection;
import com.saba.kernel.logging.SabaLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UdemyTestConnection extends VendorTestConnection {
    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdemyLogger);

    @Autowired
    private EdcastTestConnectionUtil edcastTestConnectionUtil;

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        if (accountConfigs != null) {
            // Extract credentials using constants — never hardcode key strings
            String clientId = getConfigValue(accountConfigs, UdemyConstants.CONFIG_CLIENT_ID);
            String clientSecret = getConfigValue(accountConfigs, UdemyConstants.CONFIG_CLIENT_SECRET);
            String portalId = getConfigValue(accountConfigs, UdemyConstants.PORTALID);
            String portalUrl = getConfigValue(accountConfigs, UdemyConstants.PORTALURL);

            if (clientId != null && clientSecret != null && portalId != null && portalUrl != null) {
                // Build test URL with page_size=1 to minimize data transfer
                String url = portalUrl + "/api-2.0/organizations/" + portalId + "/course-collection-sync?page_size=1";
                responses.add(testRestEndpoint(url, clientId, clientSecret));
            } else {
                return invalidConfigFailureResponse();
            }
        }

        if (!CollectionUtils.isEmpty(responses)
                && TestConnectionStatus.FAILURE.equals(responses.get(0).getStatus())
                && StringUtils.isEmpty(responses.get(0).getResponse())) {
            responses.get(0).setResponse(
                    "Test Connection Failed : Invalid Udemy Credentials");
        }

        // ALWAYS test EdCast source — every connector needs this
        String sourceId = null;
        if (accountConfigs != null && accountConfigs.get(EdCastConstants.SOURCE_NAME) != null) {
            sourceId = accountConfigs.get(EdCastConstants.SOURCE_NAME).get(0);
        }
        responses.addAll(edcastTestConnectionUtil.testEdcast(sourceId));

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
        try {
            HttpHeaders headers = getHeaders(clientId, clientSecret);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(null, headers);
            // Use framework helper — handles response parsing and status
            return getApiResponse(String.class, HttpMethod.GET, endpoint, requestEntity, null, false);
        } catch (Exception e) {
            log.error(UdemyConstants.LOG_TEST_PREFIX + "Test connection failed", e);
            TestConnectionResponse response = new TestConnectionResponse();
            response.setStatus(TestConnectionStatus.FAILURE);
            response.setResponse("Test Connection Failed: " + e.getMessage());
            return response;
        }
    }

    private String getConfigValue(Map<String, List<String>> configs, String key) {
        List<String> values = configs.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
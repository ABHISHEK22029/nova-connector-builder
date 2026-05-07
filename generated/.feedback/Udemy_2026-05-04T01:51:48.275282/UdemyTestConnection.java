package com.saba.integration.apps.udemy;

import com.saba.integration.apps.edcast.EdcastTestConnectionUtil;
import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.edcast.EdCastConstants;
import com.saba.integration.http.authentication.basic.BasicAuthenticationStrategy;
import com.saba.integration.marketplace.account.test.TestConnectionResponse;
import com.saba.integration.marketplace.account.test.TestConnectionStatus;
import com.saba.integration.marketplace.account.test.VendorTestConnection;
import com.saba.integration.marketplace.exception.MarketplaceMessage;
import com.saba.integration.marketplace.vendor.VendorConstants;
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
            // Extract credentials using constants — NEVER hardcode key strings
            String clientId = getConfigValue(accountConfigs, UdemyConstants.CONFIG_CLIENT_ID);
            String clientSecret = getConfigValue(accountConfigs, UdemyConstants.CONFIG_CLIENT_SECRET);
            String baseUrl = getConfigValue(accountConfigs, UdemyConstants.CONFIG_BASE_URL);

            if (clientId != null && clientSecret != null) {
                if (baseUrl == null || baseUrl.isEmpty()) {
                    baseUrl = UdemyConstants.DEFAULT_BASE_URL;
                }

                // Build test URL with page_size=1 to minimize data transfer
                // Using a simple endpoint like 'courses' to verify basic authentication
                String url = baseUrl + UdemyConstants.PATH_COURSES + "?" + UdemyConstants.PARAM_PAGE_SIZE + "=1";
                responses.add(testUdemyAPI(url, clientId, clientSecret));
            } else {
                responses.add(invalidConfigFailureResponse());
            }
        } else {
            responses.add(invalidConfigFailureResponse());
        }

        if (!CollectionUtils.isEmpty(responses)
                && TestConnectionStatus.FAILURE.equals(responses.get(0).getStatus())
                && StringUtils.isEmpty(responses.get(0).getResponse())) {
            responses.get(0).setResponse(
                    MarketplaceMessage.TEST_CONNECTION_FAILURE.getMessage() + " : " + UdemyConstants.ERROR_INVALID_CREDENTIALS);
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
    public String getType() {
        return VendorConstants.UDEMY;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // Empty for REST — only needed for SOAP responses
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

    private TestConnectionResponse testUdemyAPI(String endpoint, String clientId, String clientSecret) {
        try {
            log.info(UdemyConstants.LOG_TEST_PREFIX + "Testing connection to " + endpoint);
            HttpHeaders headers = getHeaders(clientId, clientSecret);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(null, headers);
            // Use framework helper — handles response parsing and status
            TestConnectionResponse apiResponse = getApiResponse(String.class, HttpMethod.GET, endpoint, requestEntity, null, false);

            if (TestConnectionStatus.SUCCESS.equals(apiResponse.getStatus())) {
                log.info(UdemyConstants.LOG_TEST_PREFIX + "Udemy API call successful - Authentication valid");
                apiResponse.setResponse("Udemy authentication successful");
            } else {
                log.error(UdemyConstants.LOG_TEST_PREFIX + "Udemy API call failed: " + apiResponse.getResponse());
                apiResponse.setResponse(UdemyConstants.ERROR_CONNECTION_FAILED + ": " + apiResponse.getResponse());
            }
            return apiResponse;
        } catch (Exception e) {
            log.error(UdemyConstants.LOG_TEST_PREFIX + "Test connection failed", e);
            TestConnectionResponse response = new TestConnectionResponse();
            response.setStatus(TestConnectionStatus.FAILURE);
            response.setResponse(MarketplaceMessage.TEST_CONNECTION_FAILURE.getMessage() + ": " + e.getMessage());
            return response;
        }
    }

    private String getConfigValue(Map<String, List<String>> configs, String key) {
        List<String> values = configs.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
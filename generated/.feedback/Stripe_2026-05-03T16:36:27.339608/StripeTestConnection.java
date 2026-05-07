package com.saba.integration.apps.stripe;

import com.saba.integration.apps.edcast.EdcastTestConnectionUtil;
import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.edcast.EdCastConstants;
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
public class StripeTestConnection extends VendorTestConnection {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.StripeLogger);

    @Autowired
    private EdcastTestConnectionUtil edcastTestConnectionUtil;

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();

        if (accountConfigs != null) {
            // Extract credentials using constants — NEVER hardcode key strings
            String apiKey = getConfigValue(accountConfigs, StripeConstants.CONFIG_API_KEY);
            String baseUrl = getConfigValue(accountConfigs, StripeConstants.CONFIG_BASE_URL);

            if (StringUtils.isEmpty(baseUrl)) {
                baseUrl = StripeConstants.DEFAULT_BASE_URL;
            }

            if (StringUtils.isNotEmpty(apiKey)) {
                // Build test URL with limit=1 to minimize data transfer
                // Using /customers endpoint as a general test for API key validity
                String url = baseUrl + StripeConstants.PATH_CUSTOMERS + "?" + StripeConstants.PARAM_LIMIT + "=1";
                responses.add(testStripeAPI(url, apiKey));
            } else {
                return invalidConfigFailureResponse();
            }
        }

        if (!CollectionUtils.isEmpty(responses)
                && TestConnectionStatus.FAILURE.equals(responses.get(0).getStatus())
                && StringUtils.isEmpty(responses.get(0).getResponse())) {
            responses.get(0).setResponse(
                    MarketplaceMessage.TEST_CONNECTION_FAILURE.getMessage() + " : " + StripeConstants.ERROR_INVALID_CREDENTIALS);
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
        return VendorConstants.STRIPE;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // No specific validation needed for REST API responses beyond HTTP status
    }

    /**
     * Builds the Authorization header for Stripe API Key.
     * Stripe uses a Bearer token scheme where the API key is the token.
     */
    private HttpHeaders getHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(StripeConstants.HEADER_AUTHORIZATION, StripeConstants.AUTH_SCHEME_BEARER + " " + apiKey);
        return headers;
    }

    /**
     * Makes a test API call to Stripe using the provided API key.
     *
     * @param endpoint The API endpoint to test.
     * @param apiKey   The Stripe API key.
     * @return A TestConnectionResponse indicating success or failure.
     */
    private TestConnectionResponse testStripeAPI(String endpoint, String apiKey) {
        try {
            log.info(StripeConstants.LOG_TEST_PREFIX + "Testing connection to " + endpoint);

            HttpHeaders headers = getHeaders(apiKey);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(null, headers);

            // Use framework helper — handles response parsing and status
            TestConnectionResponse response = getApiResponse(String.class, HttpMethod.GET, endpoint, requestEntity, null, false);

            if (TestConnectionStatus.SUCCESS.equals(response.getStatus())) {
                log.info(StripeConstants.LOG_TEST_PREFIX + "Stripe API key is valid.");
                response.setResponse("Stripe API key is valid. Connection successful.");
            } else {
                log.error(StripeConstants.LOG_TEST_PREFIX + "Stripe API key validation failed. Response: " + response.getResponse());
                response.setResponse(MarketplaceMessage.TEST_CONNECTION_FAILURE.getMessage() + ": " + StripeConstants.ERROR_INVALID_CREDENTIALS);
            }
            return response;

        } catch (Exception e) {
            log.error(StripeConstants.LOG_TEST_PREFIX + "Test connection failed", e);
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
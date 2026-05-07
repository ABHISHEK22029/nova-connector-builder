package com.saba.integration.apps.udemy;

import com.saba.integration.core.component.TestConnectionRequest;
import com.saba.integration.core.component.TestConnectionResponse;
import com.saba.integration.core.component.VendorTestConnection;
import com.saba.integration.core.util.EdcastTestConnectionUtil;
import com.saba.integration.core.vendor.VendorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.saba.integration.apps.udemy.UdemyConstants.LOG_PREFIX_TEST_CONNECTION;

@Component
public class UdemyTestConnection extends VendorTestConnection {

    @Autowired
    private EdcastTestConnectionUtil edcastTestConnectionUtil;

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        String sourceId = accountConfigs.get(VendorConstants.SOURCE_ID).get(0);

        // 1. Extract credentials from accountConfigs using constants
        String clientId = accountConfigs.get(UdemyConstants.CONFIG_CLIENT_ID).get(0);
        String clientSecret = accountConfigs.get(UdemyConstants.CONFIG_CLIENT_SECRET).get(0);
        String baseUrl = accountConfigs.get(UdemyConstants.CONFIG_BASE_URL).get(0);

        // 2. Delegate to the shared authentication logic in Flows.java to obtain an access token
        String accessToken = null;
        try {
            // This static method will be implemented in UdemyFlows.UdemyAuthenticationStrategy
            // to encapsulate the token acquisition logic and avoid duplication.
            accessToken = UdemyFlows.UdemyAuthenticationStrategy.getAccessTokenForTestConnection(
                    baseUrl, clientId, clientSecret);
        } catch (Exception e) {
            logger.error(LOG_PREFIX_TEST_CONNECTION + "Failed to obtain access token: {}", e.getMessage(), e);
            responses.add(TestConnectionResponse.failure(
                    "Failed to obtain access token: " + e.getMessage()));
            // If token acquisition fails, no further API calls can be made.
            responses.add(edcastTestConnectionUtil.testEdcast(sourceId));
            return responses;
        }

        if (accessToken == null || accessToken.isEmpty()) {
            responses.add(TestConnectionResponse.failure("Access token is null or empty after authentication attempt."));
            responses.add(edcastTestConnectionUtil.testEdcast(sourceId));
            return responses;
        }

        // 3. Build and execute a simple test API call (e.g., list courses with a small page size)
        try {
            String testUrl = baseUrl + UdemyConstants.API_COURSES_PATH + "?"
                    + UdemyConstants.QUERY_PARAM_PAGE_SIZE + "=1";

            TestConnectionRequest request = new TestConnectionRequest(testUrl);
            request.addHeader(UdemyConstants.HEADER_AUTHORIZATION, "Bearer " + accessToken);
            request.addHeader(UdemyConstants.HEADER_ACCEPT, UdemyConstants.CONTENT_TYPE_APPLICATION_JSON);

            logger.info(LOG_PREFIX_TEST_CONNECTION + "Attempting test API call to: {}", testUrl);
            TestConnectionResponse apiResponse = testRestEndpoint(request);
            responses.add(apiResponse);

        } catch (Exception e) {
            logger.error(LOG_PREFIX_TEST_CONNECTION + "Error during test API call: {}", e.getMessage(), e);
            responses.add(TestConnectionResponse.failure(
                    "Error during test API call: " + e.getMessage()));
        }

        // 4. Test EdCast source connection
        responses.add(edcastTestConnectionUtil.testEdcast(sourceId));

        return responses;
    }

    @Override
    public String getType() {
        return VendorConstants.UDEMY;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // For REST APIs, the default validation in VendorTestConnection (checking HTTP status codes)
        // is usually sufficient. Udemy API typically returns 200 for success and 401/403 for auth failures.
        // Custom validation can be added here if specific error messages in the body need to be parsed.
    }
}
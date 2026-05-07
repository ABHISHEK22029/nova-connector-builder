package com.saba.integration.apps.business;

import com.saba.integration.apps.edcast.EdcastTestConnectionUtil;
import com.saba.integration.marketplace.account.test.TestConnectionResponse;
import com.saba.integration.marketplace.account.test.TestConnectionStatus;
import com.saba.integration.marketplace.account.test.VendorTestConnection;
import com.saba.integration.marketplace.vendor.VendorConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business Test Connection
 *
 * Validates user-provided credentials (API Key) by making a test API call
 * to a known endpoint. Also tests the connection to the EdCast source.
 *
 * @author Nova Integration Team
 * @version 1.0
 */
@Component
public class BusinessTestConnection extends VendorTestConnection {

    @Autowired
    private EdcastTestConnectionUtil edcastTestConnectionUtil;

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();

        // 1. Extract credentials from accountConfigs
        String baseUrl = CollectionUtils.firstElement(accountConfigs.get(BusinessConstants.CONFIG_BASE_URL));
        String apiKey = CollectionUtils.firstElement(accountConfigs.get(BusinessConstants.CONFIG_API_KEY));
        String sourceId = CollectionUtils.firstElement(accountConfigs.get(VendorConstants.SOURCE_ID));

        // Validate required fields
        if (baseUrl == null || apiKey == null) {
            responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE, BusinessConstants.ERROR_INVALID_CREDENTIALS));
            return responses;
        }

        // 2. Build the test request URL and headers
        // For API Key authentication, we construct the URL and add the API key to headers.
        // A common endpoint like /content is used to verify connectivity and authentication.
        String testUrl = baseUrl + BusinessConstants.API_BASE_PATH + BusinessConstants.ENDPOINT_CONTENT;
        Map<String, String> headers = new HashMap<>();
        headers.put(BusinessConstants.HEADER_API_KEY, apiKey);

        // 3. Call the vendor API
        // For API Key authentication, there is no complex shared authentication strategy in Flows.java
        // that generates a session token. The API key is simply a header, so the test connection
        // directly makes the HTTP call with the provided key.
        TestConnectionResponse apiResponse = testRestEndpoint(testUrl, headers, null); // No request body for GET

        if (apiResponse.getStatus() == TestConnectionStatus.SUCCESS) {
            responses.add(apiResponse);
        } else {
            // Enhance the error message for clarity if the API call fails
            apiResponse.setMessage(BusinessConstants.ERROR_CONNECTION_FAILED + ": " + apiResponse.getMessage());
            responses.add(apiResponse);
        }

        // 4. Test EdCast source connection
        if (sourceId != null) {
            responses.add(edcastTestConnectionUtil.testEdcast(sourceId));
        } else {
            responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE, "EdCast " + VendorConstants.SOURCE_ID + " not provided. Cannot test EdCast connection."));
        }

        return responses;
    }

    @Override
    public String getType() {
        return VendorConstants.BUSINESS;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // For REST APIs, the HTTP status code (handled by testRestEndpoint) is often sufficient
        // to determine success or failure. Additional validation of the response body
        // can be added here if specific content is expected for a successful connection,
        // e.g., checking for a specific field or a non-empty data array.
        // For Business, a successful HTTP 200 response typically indicates a valid API key.
    }
}
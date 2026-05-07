package com.saba.integration.apps.udemy;

import com.saba.integration.apps.edcast.EdcastTestConnectionUtil;
import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.marketplace.account.test.TestConnectionResponse;
import com.saba.integration.marketplace.account.test.TestConnectionStatus;
import com.saba.integration.marketplace.account.test.VendorTestConnection;
import com.saba.integration.marketplace.exception.MarketplaceMessage;
import com.saba.integration.marketplace.vendor.VendorConstants;
import com.saba.kernel.logging.SabaLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Udemy Business Test Connection
 *
 * Validates user-provided credentials (Client ID, Client Secret, Portal ID, Base URL)
 * by attempting to generate a Basic Authentication header and making a test API call
 * to a simple endpoint (e.g., listing courses with a small page size).
 *
 * Authentication logic is delegated to the shared method in {@link UdemyFlows.UdemyBasicAuthenticationStrategy}.
 *
 * @author Nova Integration Team
 * @version 1.0
 */
@Component
public class UdemyTestConnection extends VendorTestConnection {

    private static final SabaLogger log = AppsLogger.getLogger(UdemyTestConnection.class);

    @Autowired
    private EdcastTestConnectionUtil edcastTestConnectionUtil;

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();

        String clientId = CollectionUtils.firstElement(accountConfigs.get(UdemyConstants.CONFIG_CLIENT_ID));
        String clientSecret = CollectionUtils.firstElement(accountConfigs.get(UdemyConstants.CONFIG_CLIENT_SECRET));
        String portalId = CollectionUtils.firstElement(accountConfigs.get(UdemyConstants.CONFIG_PORTAL_ID));
        String baseUrl = CollectionUtils.firstElement(accountConfigs.get(UdemyConstants.CONFIG_BASE_URL));

        // Validate required credentials
        if (StringUtils.isAnyBlank(clientId, clientSecret, portalId, baseUrl)) {
            log.error(UdemyConstants.LOG_TEST_PREFIX + "Missing required credentials for Udemy Business connection.");
            responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE,
                    MarketplaceMessage.ERROR_MISSING_REQUIRED_CREDENTIALS.getMessage()));
            responses.add(edcastTestConnectionUtil.testEdcast(UdemyConstants.UDEMY));
            return responses;
        }

        try {
            log.info(UdemyConstants.LOG_TEST_PREFIX + "Attempting to validate Udemy Business credentials.");

            // Delegate authentication logic to the shared method in Flows.java
            // This method should internally make a test API call to validate credentials
            String authHeader = UdemyFlows.UdemyBasicAuthenticationStrategy.generateBasicAuthHeader(
                    baseUrl, clientId, clientSecret, portalId);

            if (StringUtils.isNotBlank(authHeader)) {
                log.info(UdemyConstants.LOG_TEST_PREFIX + "Udemy Business authentication successful.");
                responses.add(new TestConnectionResponse(TestConnectionStatus.SUCCESS,
                        "Udemy Business authentication successful."));
            } else {
                // This path should ideally not be hit if generateBasicAuthHeader throws an exception on failure
                // but included as a safeguard.
                log.error(UdemyConstants.LOG_TEST_PREFIX + "Failed to generate valid Udemy Business authentication header. Invalid credentials or API error.");
                responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE,
                        UdemyConstants.ERROR_INVALID_CREDENTIALS));
            }

        } catch (Exception e) {
            log.error(UdemyConstants.LOG_TEST_PREFIX + "Error during Udemy Business test connection: " + e.getMessage(), e);
            responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE,
                    UdemyConstants.ERROR_CONNECTION_FAILED + ": " + e.getMessage()));
        }

        // Always test EdCast source connection
        responses.add(edcastTestConnectionUtil.testEdcast(UdemyConstants.UDEMY));

        return responses;
    }

    @Override
    public String getType() {
        return VendorConstants.UDEMY;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // No specific validation needed for REST API responses beyond HTTP status code.
        // The UdemyBasicAuthenticationStrategy handles the actual API call and validation.
    }
}
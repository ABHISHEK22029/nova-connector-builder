package com.saba.integration.apps.udemy;

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
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Udemy Test Connection
 * <p>
 * Validates the user's credentials by attempting to obtain an OAuth2 access token
 * using the client_credentials grant type.
 * </p>
 *
 * @author Nova Integration Team
 * @version 1.0
 */
@Component
public class UdemyTestConnection extends VendorTestConnection {

    private static final SabaLogger LOGGER = SabaLogger.getLogger(UdemyTestConnection.class);

    @Autowired
    private EdcastTestConnectionUtil edcastTestConnectionUtil;

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        String sourceId = getSourceId(accountConfigs); // Inherited from VendorTestConnection

        String baseUrl = getAccountConfigValue(accountConfigs, UdemyConstants.CONFIG_BASE_URL);
        String clientId = getAccountConfigValue(accountConfigs, UdemyConstants.CONFIG_CLIENT_ID);
        String clientSecret = getAccountConfigValue(accountConfigs, UdemyConstants.CONFIG_CLIENT_SECRET);
        String portalId = getAccountConfigValue(accountConfigs, UdemyConstants.CONFIG_PORTAL_ID);

        LOGGER.info(UdemyConstants.LOG_TEST_PREFIX + "Attempting to test connection for sourceId: {}", sourceId);

        // Validate required fields
        if (StringUtils.isBlank(baseUrl) || StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
            responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE,
                    UdemyConstants.ERROR_INVALID_CREDENTIALS));
            LOGGER.error(UdemyConstants.LOG_TEST_PREFIX + "Missing required credentials (Base URL, Client ID, or Client Secret) for sourceId: {}", sourceId);
            // Always test EdCast source connection even if vendor connection fails
            responses.addAll(edcastTestConnectionUtil.testEdcast(sourceId));
            return responses;
        }

        if (StringUtils.isBlank(portalId)) {
            responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE,
                    UdemyConstants.ERROR_MISSING_PORTAL_ID));
            LOGGER.error(UdemyConstants.LOG_TEST_PREFIX + "Missing Portal ID for sourceId: {}", sourceId);
            // Always test EdCast source connection even if vendor connection fails
            responses.addAll(edcastTestConnectionUtil.testEdcast(sourceId));
            return responses;
        }

        try {
            // Delegate authentication logic to the shared method in Flows.java
            // This method will attempt to get an access token
            String accessToken = UdemyFlows.UdemyClientCredentialsAuthStrategy.generateAccessToken(
                    baseUrl, clientId, clientSecret, portalId);

            if (StringUtils.isNotBlank(accessToken)) {
                responses.add(new TestConnectionResponse(TestConnectionStatus.SUCCESS, "Successfully obtained access token."));
                LOGGER.info(UdemyConstants.LOG_TEST_PREFIX + "Successfully obtained access token for sourceId: {}", sourceId);
            } else {
                // This case should ideally be caught by the exception, but as a fallback
                responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE,
                        UdemyConstants.ERROR_TOKEN_GENERATION_FAILED));
                LOGGER.error(UdemyConstants.LOG_TEST_PREFIX + "Failed to obtain access token for sourceId: {}. Token was blank.", sourceId);
            }
        } catch (MarketplaceMessage e) {
            responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE, e.getMessage()));
            LOGGER.error(UdemyConstants.LOG_TEST_PREFIX + "Authentication failed for sourceId: {}. Error: {}", sourceId, e.getMessage(), e);
        } catch (Exception e) {
            responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE,
                    UdemyConstants.ERROR_CONNECTION_FAILED + ": " + e.getMessage()));
            LOGGER.error(UdemyConstants.LOG_TEST_PREFIX + "An unexpected error occurred during connection test for sourceId: {}. Error: {}", sourceId, e.getMessage(), e);
        }

        // Always test EdCast source connection
        responses.addAll(edcastTestConnectionUtil.testEdcast(sourceId));

        return responses;
    }

    @Override
    public String getType() {
        return VendorConstants.UDEMY;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // No specific validation needed for REST API token generation success
    }

    /**
     * Helper method to safely extract a single value from account configurations.
     *
     * @param accountConfigs The map of account configurations.
     * @param key            The key for the configuration value.
     * @return The first value associated with the key, or null if not found or empty.
     */
    private String getAccountConfigValue(Map<String, List<String>> accountConfigs, String key) {
        List<String> values = accountConfigs.get(key);
        return CollectionUtils.isEmpty(values) ? null : values.get(0);
    }
}
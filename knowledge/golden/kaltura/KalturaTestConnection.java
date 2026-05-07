/*
 * ============================================================================
 * GOLDEN REFERENCE: KalturaTestConnection.java
 * ============================================================================
 * AUTH TYPE: Session Token (KS)
 * 
 * CRITICAL PATTERN RULES:
 * 1. Must extend VendorTestConnection
 * 2. Must be annotated with @Component (Spring auto-discovery)
 * 3. getType() must return the constant from VendorConstants.java
 * 4. Extract ALL credentials from accountConfigs map using getConfigValue()
 * 5. NEVER duplicate auth logic — delegate to the shared method in Flows.java
 *    (KalturaFlows.KalturaSessionAuthenticationStrategy.generateKalturaSession)
 * 6. Always test EdCast source connection via edcastTestConnectionUtil
 * 7. Return proper TestConnectionResponse with SUCCESS/FAILURE status
 * 8. Use constants for all string values — never hardcode
 * 
 * FOR OAUTH2 CONNECTORS:
 *   - POST to token URL with client_credentials grant
 *   - If 200 + valid token → SUCCESS
 * FOR BASIC AUTH:
 *   - Build Authorization header, GET a test endpoint
 *   - If 200 → SUCCESS
 * FOR API KEY:
 *   - Add key as header, GET a test endpoint
 *   - If 200 → SUCCESS
 * ============================================================================
 */
package com.saba.integration.apps.kaltura;

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

@Component
public class KalturaTestConnection extends VendorTestConnection {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.KalturaLogger);

    @Autowired
    private EdcastTestConnectionUtil edcastTestConnectionUtil;

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();

        if (accountConfigs != null) {
            // Extract credentials using constants — NEVER hardcode key strings
            String partnerId = getConfigValue(accountConfigs, KalturaConstants.CONFIG_PARTNER_ID);
            String adminSecret = getConfigValue(accountConfigs, KalturaConstants.CONFIG_ADMIN_SECRET);
            String sessionTypeStr = getConfigValue(accountConfigs, KalturaConstants.CONFIG_SESSION_TYPE);
            String baseUrl = getConfigValue(accountConfigs, KalturaConstants.CONFIG_BASE_URL);

            if (partnerId != null && adminSecret != null) {
                int sessionType = KalturaConstants.SESSION_TYPE_ADMIN;
                if (sessionTypeStr != null) {
                    try {
                        sessionType = Integer.parseInt(sessionTypeStr);
                    } catch (NumberFormatException e) {
                        log.debug(KalturaConstants.LOG_TEST_PREFIX + "Invalid session type, using default: 2 (Admin)");
                    }
                }

                if (baseUrl == null || baseUrl.isEmpty()) {
                    baseUrl = KalturaConstants.DEFAULT_BASE_URL;
                }

                responses.add(testKalturaAPI(baseUrl, partnerId, adminSecret, sessionType));
            } else {
                return invalidConfigFailureResponse();
            }
        }

        if (!CollectionUtils.isEmpty(responses)
                && TestConnectionStatus.FAILURE.equals(responses.get(0).getStatus())
                && StringUtils.isEmpty(responses.get(0).getResponse())) {
            responses.get(0).setResponse(
                    MarketplaceMessage.TEST_CONNECTION_FAILURE.getMessage() + " : Invalid Kaltura Credentials");
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
        return VendorConstants.KALTURA;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // Empty for REST — only needed for SOAP responses
    }

    private TestConnectionResponse testKalturaAPI(String baseUrl, String partnerId,
            String secret, int sessionType) {
        try {
            log.info(KalturaConstants.LOG_TEST_PREFIX + "Testing connection to " + baseUrl);

            /*
             * KEY PATTERN: Delegate to the SHARED auth method in Flows.java
             * This is the DRY principle — auth logic exists in ONE place only
             */
            String ks = KalturaFlows.KalturaSessionAuthenticationStrategy
                    .generateKalturaSession(baseUrl, partnerId, secret, sessionType);

            if (ks == null || ks.isEmpty()) {
                log.error(KalturaConstants.LOG_TEST_PREFIX + "Failed to generate KS token");
                TestConnectionResponse response = new TestConnectionResponse();
                response.setStatus(TestConnectionStatus.FAILURE);
                response.setResponse("Failed to generate Kaltura Session token. Invalid credentials.");
                return response;
            }

            log.info(KalturaConstants.LOG_TEST_PREFIX + "KS token generated - Authentication valid");
            TestConnectionResponse response = new TestConnectionResponse();
            response.setStatus(TestConnectionStatus.SUCCESS);
            response.setResponse("Kaltura authentication successful");
            return response;

        } catch (Exception e) {
            log.error(KalturaConstants.LOG_TEST_PREFIX + "Test connection failed", e);
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

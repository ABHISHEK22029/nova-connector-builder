package com.saba.integration.apps.successfactor;

import com.saba.integration.apps.edcast.EdcastTestConnectionUtil;
import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.edcast.EdCastConstants;
import com.saba.integration.marketplace.account.test.TestConnectionResponse;
import com.saba.integration.marketplace.account.test.TestConnectionStatus;
import com.saba.integration.marketplace.account.test.VendorTestConnection;
import com.saba.integration.marketplace.exception.MarketplaceMessage;
import com.saba.integration.marketplace.vendor.VendorConstants;
import com.saba.kernel.logging.SabaLogger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SuccessfactorTestConnection extends VendorTestConnection {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.SuccessfactorLogger);

    @Autowired
    private EdcastTestConnectionUtil edcastTestConnectionUtil;

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();

        if (accountConfigs != null) {
            // Extract credentials using constants — NEVER hardcode key strings
            String baseUrl = getConfigValue(accountConfigs, SuccessfactorConstants.CONFIG_BASE_URL);
            String companyId = getConfigValue(accountConfigs, SuccessfactorConstants.CONFIG_COMPANY_ID);
            String username = getConfigValue(accountConfigs, SuccessfactorConstants.CONFIG_USERNAME);
            String password = getConfigValue(accountConfigs, SuccessfactorConstants.CONFIG_PASSWORD);

            if (companyId != null && username != null && password != null && baseUrl != null) {
                responses.add(testSuccessfactorAPI(baseUrl, companyId, username, password));
            } else {
                return invalidConfigFailureResponse();
            }
        }

        if (!CollectionUtils.isEmpty(responses)
                && TestConnectionStatus.FAILURE.equals(responses.get(0).getStatus())
                && StringUtils.isEmpty(responses.get(0).getResponse())) {
            responses.get(0).setResponse(
                    MarketplaceMessage.TEST_CONNECTION_FAILURE.getMessage() + " : Invalid SuccessFactors Credentials");
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
        return VendorConstants.SUCCESSFACTOR;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // Empty for REST — only needed for SOAP responses
    }

    private TestConnectionResponse testSuccessfactorAPI(String baseUrl, String companyId,
            String username, String password) {
        try {
            log.info(SuccessfactorConstants.LOG_TEST_PREFIX + "Testing connection to " + baseUrl);

            /*
             * KEY PATTERN: Delegate to the SHARED auth method in Flows.java
             * This is the DRY principle — auth logic exists in ONE place only
             */
            String apiUrl = baseUrl + SuccessfactorConstants.ODATA_ENDPOINT + "/User?$top=1";

            HttpHeaders headers = getHeaders(username + "@" + companyId, password);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(null, headers);

            TestConnectionResponse response = getApiResponse(String.class, HttpMethod.GET, apiUrl, requestEntity, null, false);

            if (TestConnectionStatus.SUCCESS.equals(response.getStatus())) {
                response.setResponse("SuccessFactors authentication successful");
            } else {
                response.setResponse("Failed to connect to SuccessFactors. Invalid credentials.");
            }

            return response;

        } catch (Exception e) {
            log.error(SuccessfactorConstants.LOG_TEST_PREFIX + "Test connection failed", e);
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

    private HttpHeaders getHeaders(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        String authHeader = SuccessfactorConstants.AUTH_BASIC + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set(SuccessfactorConstants.HEADER_AUTHORIZATION, authHeader);
        headers.set(SuccessfactorConstants.HEADER_ACCEPT, "application/json");
        return headers;
    }
}
```
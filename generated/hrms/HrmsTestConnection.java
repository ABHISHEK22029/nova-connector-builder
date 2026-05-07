package com.saba.integration.apps.hrms;

import com.saba.integration.apps.commons.apps.AbstractTestConnection;
import com.saba.integration.apps.commons.apps.AppsLogger;
import com.saba.integration.apps.commons.apps.MarketplaceMessage;
import com.saba.integration.apps.commons.apps.TestConnectionResponse;
import com.saba.integration.apps.commons.apps.TestConnectionStatus;
import com.saba.integration.apps.commons.logging.SabaLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HrmsTestConnection extends AbstractTestConnection {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.HrmsLogger);

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (accountConfigs != null) {
            String apiKey = accountConfigs.get(HrmsConstants.API_KEY) != null ? accountConfigs.get(HrmsConstants.API_KEY).getFirst() : null;
            String companyId = accountConfigs.get(HrmsConstants.COMPANY_ID) != null ? accountConfigs.get(HrmsConstants.COMPANY_ID).getFirst() : null;
            if(log.isDebugEnabled()) {
                log.debug("#HrmsTestConnection: value for apiKey is:" + apiKey);
                log.debug("#HrmsTestConnection: value for companyId is:" + companyId);
            }
            if (StringUtils.isNotEmpty(apiKey) && StringUtils.isNotEmpty(companyId)) {
                responses.add(testRestEndpoint(apiKey, companyId));
                // Set error message in response when invalid client secret is provided because we are getting blank response body
                if (TestConnectionStatus.FAILURE.equals(responses.getFirst().getStatus())) {
                    responses.getFirst().setResponse(
                            MarketplaceMessage.TEST_CONNECTION_FAILURE.getMessage() + " : Invalid Credentials Provided"
                    );
                }
                return responses;
            }

        }
        return invalidConfigFailureResponse();
    }

    private TestConnectionResponse testRestEndpoint(String apiKey, String companyId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        headers.set("X-Company-Id", companyId);

        HttpEntity requestEntity = new HttpEntity<>(null, headers);
        String endpoint = HrmsConstants.DEFAULT_BASE_URL + HrmsConstants.EMPLOYEE_ENDPOINT + "?limit=1"; // Test with a simple API call
        return getApiResponse(String.class, HttpMethod.GET, endpoint, requestEntity, null, false);
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // No specific validation needed for basic auth response
    }
}
package com.saba.integration.apps.relly;

import com.saba.integration.apps.commons.apps.AppsLogger;
import com.saba.integration.apps.commons.base.AbstractTestConnection;
import com.saba.integration.apps.commons.message.MarketplaceMessage;
import com.saba.integration.apps.commons.model.testconnection.TestConnectionResponse;
import com.saba.integration.apps.commons.model.testconnection.TestConnectionStatus;
import com.saba.integration.apps.commons.utils.SabaLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RellyTestConnection extends AbstractTestConnection {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.RellyLogger);

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (accountConfigs != null) {
            String apiKey = accountConfigs.get(RellyConstants.API_KEY) != null ? accountConfigs.get(RellyConstants.API_KEY).getFirst() : null;
            String baseUrl = accountConfigs.get(RellyConstants.RELLY_BASE_URL) != null ? accountConfigs.get(RellyConstants.RELLY_BASE_URL).getFirst() : null;
            if(log.isDebugEnabled()) {
                log.debug("#RellyTestConnection: value for apiKey is:" + apiKey);
                log.debug("#RellyTestConnection: value for baseUrl is:" + baseUrl);
            }
            if (StringUtils.isNotEmpty(apiKey) && StringUtils.isNotEmpty(baseUrl)) {
                responses.add(testRestEndpoint(baseUrl, apiKey));
                return responses;
            }

        }
        return invalidConfigFailureResponse();
    }

    private TestConnectionResponse testRestEndpoint(String baseUrl, String apiKey) {
        HttpHeaders headers = getHeaders(apiKey);

        HttpEntity requestEntity = new HttpEntity<>(null, headers);
        String endpoint = baseUrl + "/api/v1/ping";
        return getApiResponse(String.class, HttpMethod.GET, endpoint, requestEntity, null, false);
    }

    private HttpHeaders getHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        return headers;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        if (response.getStatus() == TestConnectionStatus.SUCCESS && !StringUtils.equals(body, "pong")) {
            response.setStatus(TestConnectionStatus.FAILURE);
            response.setResponse(MarketplaceMessage.TEST_CONNECTION_FAILURE.getMessage() + " : Unexpected response from Relly API");
        }
    }
}
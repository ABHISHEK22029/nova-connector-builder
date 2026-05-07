package com.saba.integration.apps.udemy;

import com.saba.integration.apps.AppsLogger;
import com.saba.integration.core.api.ServiceLocator;
import com.saba.integration.core.exception.AppsException;
import com.saba.integration.core.model.TestConnectionResponse;
import com.saba.integration.core.model.TestConnectionStatus;
import com.saba.integration.core.request.AbstractTestConnection;
import com.saba.integration.core.string.StringUtils;
import com.saba.integration.core.util.MarketplaceMessage;
import com.saba.integration.logging.SabaLogger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UdemyTestConnection extends AbstractTestConnection {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdemyLogger);

    private static final String CLIENT_ID = UdemyConstants.CONFIG_CLIENT_ID;
    private static final String CLIENT_SECRET = UdemyConstants.CONFIG_CLIENT_SECRET;

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (accountConfigs != null) {
            String clientId = accountConfigs.get(CLIENT_ID) != null ? accountConfigs.get(CLIENT_ID).getFirst() : null;
            String clientSecret = accountConfigs.get(CLIENT_SECRET) != null ? accountConfigs.get(CLIENT_SECRET).getFirst() : null;
            if(log.isDebugEnabled()) {
                log.debug("#UdemyTestConnection: value for clientId is:" + clientId);
            }
            if (StringUtils.isNotEmpty(clientId) && StringUtils.isNotEmpty(clientSecret)) {
                responses.add(testRestEndpoint(clientId, clientSecret));
                // Set error message in response when invalid client secret is provided because we are getting blank response body
                if (TestConnectionStatus.FAILURE.equals(responses.getFirst().getStatus()) && StringUtils.isEmpty(responses.getFirst().getResponse())) {
                    responses.getFirst().setResponse(
                            MarketplaceMessage.TEST_CONNECTION_FAILURE.getMessage() + " : Invalid Credentials Provided"
                    );
                }
                return responses;
            }

        }
        return invalidConfigFailureResponse();
    }

    private TestConnectionResponse testRestEndpoint(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = getHeaders(UdemyConstants.AUTH_TYPE + encodedCredentials);

        HttpEntity requestEntity = new HttpEntity<>(null, headers);
        return getApiResponse(String.class, HttpMethod.GET, UdemyConstants.DEFAULT_BASE_URL + UdemyConstants.COURSES_ENDPOINT + "?page_size=1", requestEntity, null, false);
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // No specific validation needed for basic auth response
    }
}
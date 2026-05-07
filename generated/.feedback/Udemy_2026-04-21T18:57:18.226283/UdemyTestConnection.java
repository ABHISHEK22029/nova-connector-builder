package com.saba.integration.apps.udemy;

import com.saba.integration.apps.udemy.util.AppsLogger;
import com.saba.integration.core.api.ServiceLocator;
import com.saba.integration.core.exception.AppsException;
import com.saba.integration.core.model.TestConnectionResponse;
import com.saba.integration.core.model.TestConnectionStatus;
import com.saba.integration.core.rest.AbstractTestConnection;
import com.saba.integration.core.util.MarketplaceMessage;
import com.saba.integration.core.util.SabaLogger;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (accountConfigs != null) {
            String clientId = accountConfigs.get(UdemyConstants.CONFIG_CLIENT_ID) != null ? accountConfigs.get(UdemyConstants.CONFIG_CLIENT_ID).getFirst() : null;
            String clientSecret = accountConfigs.get(UdemyConstants.CONFIG_CLIENT_SECRET) != null ? accountConfigs.get(UdemyConstants.CONFIG_CLIENT_SECRET).getFirst() : null;
            if(log.isDebugEnabled()) {
                log.debug("#UdemyTestConnection: value for clientId is:" + clientId);
            }
            if (StringUtils.isNotEmpty(clientId) && StringUtils.isNotEmpty(clientSecret)) {
                String auth = UdemyConstants.AUTH_TYPE + java.util.Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
                responses.add(testRestEndpoint(auth));
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

    private TestConnectionResponse testRestEndpoint(String udemyAuthToken) {
        HttpHeaders headers = getHeaders(udemyAuthToken);

        HttpEntity requestEntity = new HttpEntity<>(null, headers);
        return getApiResponse(String.class, HttpMethod.GET, UdemyConstants.DEFAULT_BASE_URL + UdemyConstants.COURSES_ENDPOINT, requestEntity, null, false);
    }

    private HttpHeaders getHeaders(String udemyAuthToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(UdemyConstants.AUTH_HEADER, udemyAuthToken);
        return headers;
    }
}
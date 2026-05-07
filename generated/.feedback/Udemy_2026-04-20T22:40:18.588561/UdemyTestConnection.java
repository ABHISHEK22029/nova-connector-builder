package com.saba.integration.apps.udemy;

import com.saba.integration.apps.udemy.UdemyConstants;
import com.saba.integration.core.api.AppsLogger;
import com.saba.integration.core.api.ServiceLocator;
import com.saba.integration.core.exception.AppsException;
import com.saba.integration.core.model.TestConnectionResponse;
import com.saba.integration.core.model.TestConnectionStatus;
import com.saba.integration.core.rest.Oauth2AuthenticationConstants;
import com.saba.integration.framework.api.connectors.AbstractTestConnection;
import com.saba.integration.framework.i18n.MarketplaceMessage;
import com.saba.integration.utils.SabaLogger;
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

    private TestConnectionResponse testRestEndpoint(String clientId, String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        String authHeaderValue = UdemyConstants.AUTH_TYPE + encodedCredentials;

        HttpHeaders headers = new HttpHeaders();
        headers.set(UdemyConstants.AUTH_HEADER, authHeaderValue);

        HttpEntity requestEntity = new HttpEntity<>(null, headers);
        return getApiResponse(String.class, HttpMethod.GET, UdemyConstants.DEFAULT_BASE_URL + UdemyConstants.COURSES_ENDPOINT + "?page_size=1", requestEntity, null, false);
    }

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        if (accountConfigs != null) {
            String clientId = accountConfigs.get(UdemyConstants.CONFIG_CLIENT_ID) != null ? accountConfigs.get(UdemyConstants.CONFIG_CLIENT_ID).getFirst() : null;
            String clientSecret = accountConfigs.get(UdemyConstants.CONFIG_CLIENT_SECRET) != null ? accountConfigs.get(UdemyConstants.CONFIG_CLIENT_SECRET).getFirst() : null;
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
}
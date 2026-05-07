package com.saba.integration.apps.udacity;

import com.saba.integration.apps.AppsLogger;
import com.saba.integration.apps.MarketplaceMessage;
import com.saba.integration.framework.async.ServiceLocator;
import com.saba.integration.framework.oauth2.Oauth2AuthenticationConstants;
import com.saba.integration.framework.testconnection.AbstractTestConnection;
import com.saba.integration.framework.testconnection.TestConnectionResponse;
import com.saba.integration.framework.testconnection.TestConnectionStatus;
import com.saba.integration.utils.SabaLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class UdacityTestConnection extends AbstractTestConnection {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdacityLogger);


    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        if (accountConfigs != null) {
            String clientId = accountConfigs.get(UdacityConstants.CLIENT_ID) != null ? accountConfigs.get(UdacityConstants.CLIENT_ID).getFirst() : null;
            String clientSecret = accountConfigs.get(UdacityConstants.CLIENT_SECRET) != null ? accountConfigs.get(UdacityConstants.CLIENT_SECRET).getFirst() : null;
            if(log.isDebugEnabled()) {
                log.debug("#UdacityTestConnection: value for clientId is:" + clientId);
            }
            if (StringUtils.isNotEmpty(clientId) && StringUtils.isNotEmpty(clientSecret)) {
                map.add(Oauth2AuthenticationConstants.GRANT_TYPE_CLIENT_ID, clientId);
                map.add(Oauth2AuthenticationConstants.GRANT_TYPE_CLIENT_SECRET, clientSecret);
                map.add(Oauth2AuthenticationConstants.GRANT_TYPE_CLIENT_GRANT_TYPE, "client_credentials");
                responses.add(testRestEndpoint(map));
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

    private TestConnectionResponse testRestEndpoint(MultiValueMap<String, String> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity requestEntity = new HttpEntity<>(map, headers);
        return getApiResponse(String.class, HttpMethod.POST, "/oauth/token", requestEntity, null, false);
    }
}
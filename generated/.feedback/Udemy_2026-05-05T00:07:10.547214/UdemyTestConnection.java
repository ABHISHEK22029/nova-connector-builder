package com.saba.integration.apps.udemy;

import com.saba.integration.apps.edcast.EdcastTestConnectionUtil;
import com.saba.integration.apps.udemy.UdemyConstants;
import com.saba.integration.apps.udemy.UdemyFlows;
import com.saba.integration.edcast.EdCastConstants;
import com.saba.integration.marketplace.account.test.TestConnectionResponse;
import com.saba.integration.marketplace.account.test.TestConnectionStatus;
import com.saba.integration.marketplace.account.test.VendorTestConnection;
import com.saba.integration.marketplace.exception.MarketplaceMessage;
import com.saba.integration.marketplace.vendor.VendorConstants;
import com.saba.kernel.logging.SabaLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class UdemyTestConnection extends VendorTestConnection {

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();
        String clientId = accountConfigs.get(UdemyConstants.CLIENT_ID).get(0);
        String clientSecret = accountConfigs.get(UdemyConstants.CLIENT_SECRET).get(0);
        String accessToken = UdemyFlows.UdemyAuthStrategy.generateAccessToken(clientId, clientSecret);
        if (StringUtils.isEmpty(accessToken)) {
            responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE, "Failed to generate access token"));
            return responses;
        }
        String url = UdemyConstants.TEST_ENDPOINT;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(UdemyConstants.AUTHORIZATION, "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                responses.add(new TestConnectionResponse(TestConnectionStatus.SUCCESS, "Test connection successful"));
                EdcastTestConnectionUtil.testEdcast(accountConfigs.get(UdemyConstants.SOURCE_ID).get(0));
            } else {
                responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE, "Test connection failed"));
            }
        } catch (Exception e) {
            responses.add(new TestConnectionResponse(TestConnectionStatus.FAILURE, "Test connection failed with error: " + e.getMessage()));
        }
        return responses;
    }

    @Override
    public String getType() {
        return VendorConstants.UDEMY;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // No specific validation needed for Udemy test connection response
    }
}
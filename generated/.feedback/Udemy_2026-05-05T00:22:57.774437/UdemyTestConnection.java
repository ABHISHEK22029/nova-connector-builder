package com.saba.integration.apps.udemy;

import com.saba.integration.apps.edcast.EdcastTestConnectionUtil;
import com.saba.integration.marketplace.account.test.TestConnectionResponse;
import com.saba.integration.marketplace.account.test.TestConnectionStatus;
import com.saba.integration.marketplace.account.test.VendorTestConnection;
import com.saba.integration.marketplace.vendor.VendorConstants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UdemyTestConnection extends VendorTestConnection {

    @Override
    public List<TestConnectionResponse> testConnection(Map<String, List<String>> accountConfigs) {
        List<TestConnectionResponse> responses = new ArrayList<>();

        String clientId = accountConfigs.get(UdemyConstants.CONFIG_CLIENT_ID).get(0);
        String clientSecret = accountConfigs.get(UdemyConstants.CONFIG_CLIENT_SECRET).get(0);
        String baseUrl = accountConfigs.get(UdemyConstants.CONFIG_BASE_URL).get(0);

        TestConnectionResponse response = UdemyFlows.UdemyAuthenticationStrategy.testUdemyConnection(baseUrl, clientId, clientSecret);
        responses.add(response);

        String sourceId = accountConfigs.get(EdCastConstants.EDCAST_SOURCE_ID).get(0);
        responses.addAll(EdcastTestConnectionUtil.testEdcast(sourceId));

        return responses;
    }

    @Override
    public String getType() {
        return VendorConstants.UDEMY;
    }

    @Override
    protected void validateResponse(TestConnectionResponse response, String body) {
        // No specific validation needed for REST responses in this case
    }
}
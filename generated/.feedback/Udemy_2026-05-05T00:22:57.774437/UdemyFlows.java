package com.saba.integration.apps.udemy;

import com.saba.integration.apps.util.FlowGraphCreator;
import com.saba.integration.component.IntegrationSource;
import com.saba.integration.http.authentication.AbstractReusableAuthStrategy;
import com.saba.integration.message.Message;
import com.saba.integration.stream.IntegrationStreamNode;
import com.saba.integration.stream.flow.IntegrationFlow;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.saba.integration.marketplace.account.test.TestConnectionResponse;
import com.saba.integration.marketplace.account.test.TestConnectionStatus;

@Configuration
public class UdemyFlows {

    @Bean(name = "integration.udemy.import.course")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow courseImportFlow(String id,
                                            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        return FlowGraphCreator.generateIntegrationFlow(
                "/com/saba/mapping/udemy/flow/Content.js",
                "integration.udemy.import.course",
                sourceNodeMap, id, true);
    }

    @Bean(name = "integration.udemy.import.user")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow userImportFlow(String id,
                                            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        return FlowGraphCreator.generateIntegrationFlow(
                "/com/saba/mapping/udemy/flow/Content.js",
                "integration.udemy.import.user",
                sourceNodeMap, id, true);
    }

    static class UdemyAuthenticationStrategy extends AbstractReusableAuthStrategy {

        @Override
        public MultiValueMap<String, String> getAuthHeaders(Message message, Map<String, Object> detail) {
            return generateAuthHeaders(message, detail);
        }

        private static MultiValueMap<String, String> generateAuthHeaders(Message message, Map<String, Object> detail) {
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            String clientId = (String) detail.get(UdemyConstants.CONFIG_CLIENT_ID);
            String clientSecret = (String) detail.get(UdemyConstants.CONFIG_CLIENT_SECRET);

            if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
                throw new IllegalArgumentException("Client ID and Client Secret must be provided.");
            }

            String auth = clientId + ":" + clientSecret;
            byte[] encodedAuth = Base64Utils.encode(auth.getBytes(StandardCharsets.UTF_8));
            String authHeader = UdemyConstants.AUTH_BASIC + new String(encodedAuth);

            headers.add(HttpHeaders.AUTHORIZATION, authHeader);
            headers.add(HttpHeaders.ACCEPT, UdemyConstants.ACCEPT_JSON);
            headers.add(HttpHeaders.CONTENT_TYPE, UdemyConstants.CONTENT_TYPE_JSON);

            return headers;
        }

        static TestConnectionResponse testUdemyConnection(String baseUrl, String clientId, String clientSecret) {
            TestConnectionResponse response = new TestConnectionResponse();
            try {
                if (StringUtils.isEmpty(baseUrl) || StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
                    response.setStatus(TestConnectionStatus.FAILURE);
                    response.setMessage(UdemyConstants.LOG_TEST_PREFIX + "Base URL, Client ID, and Client Secret must be provided.");
                    return response;
                }

                MultiValueMap<String, String> detail = new LinkedMultiValueMap<>();
                detail.add(UdemyConstants.CONFIG_CLIENT_ID, clientId);
                detail.add(UdemyConstants.CONFIG_CLIENT_SECRET, clientSecret);

                MultiValueMap<String, String> headers = generateAuthHeaders(null, (Map) detail);

                if (headers != null && !headers.isEmpty() && headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                    response.setStatus(TestConnectionStatus.SUCCESS);
                    response.setMessage(UdemyConstants.LOG_TEST_PREFIX + "Successfully authenticated with Udemy.");
                } else {
                    response.setStatus(TestConnectionStatus.FAILURE);
                    response.setMessage(UdemyConstants.LOG_TEST_PREFIX + "Authentication with Udemy failed.");
                }

            } catch (Exception e) {
                response.setStatus(TestConnectionStatus.FAILURE);
                response.setMessage(UdemyConstants.LOG_TEST_PREFIX + "An error occurred during the test connection: " + e.getMessage());
            }
            return response;
        }
    }
}
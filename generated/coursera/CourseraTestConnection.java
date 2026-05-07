package com.saba.integration.apps.coursera;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.saba.integration.apps.commons.MarketplaceMessage;
import com.saba.integration.framework.async.TestConnectionResponse;
import com.saba.integration.framework.async.TestConnectionStatus;
import com.saba.integration.framework.connectors.AbstractTestConnection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CourseraTestConnection extends AbstractTestConnection {

    @Override
    protected TestConnectionResponse testConnection(Map<String, List<String>> configs) {
        String baseUrl = getConfigValue(configs, CourseraConstants.COURSERA_BASE_URL);
        String accessToken = getConfigValue(configs, CourseraConstants.ACCESS_TOKEN);

        if (baseUrl == null || baseUrl.isEmpty() || accessToken == null || accessToken.isEmpty()) {
            TestConnectionResponse response = new TestConnectionResponse();
            response.setStatus(TestConnectionStatus.FAILURE);
            response.setResponse("Base URL and Access Token must be configured.");
            return response;
        }

        return testCourseraAPI(baseUrl, accessToken);
    }

    private TestConnectionResponse testCourseraAPI(String baseUrl, String accessToken) {
        try {
            log.info("Testing connection to Coursera API at " + baseUrl);

            String endpoint = baseUrl + "/api/catalog/v1/courses";

            HttpHeaders headers = new HttpHeaders();
            headers.set(CourseraConstants.AUTHORIZATION, "Bearer " + accessToken);

            HttpEntity requestEntity = new HttpEntity<>(null, headers);

            TestConnectionResponse response = getApiResponse(String.class, HttpMethod.GET, endpoint, requestEntity, null, false);

            if (response.getStatus() == TestConnectionStatus.SUCCESS) {
                response.setResponse("Successfully connected to Coursera API");
            } else {
                response.setResponse("Failed to connect to Coursera API. Check your credentials and base URL.");
            }

            return response;

        } catch (Exception e) {
            log.error("Test connection failed", e);
            TestConnectionResponse response = new TestConnectionResponse();
            response.setStatus(TestConnectionStatus.FAILURE);
            response.setResponse(MarketplaceMessage.TEST_CONNECTION_FAILURE.getMessage() + ": " + e.getMessage());
            return response;
        }
    }

    /**
     * Get configuration value from map
     */
    private String getConfigValue(Map<String, List<String>> configs, String key) {
        List<String> values = configs.get(key);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
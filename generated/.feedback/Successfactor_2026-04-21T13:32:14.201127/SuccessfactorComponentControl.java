package com.saba.integration.apps.successfactor;

import com.saba.integration.core.model.ConnectorContext;
import com.saba.integration.core.model.HttpRequest;
import com.saba.integration.core.model.HttpResponse;
import com.saba.integration.core.service.AbstractComponentControl;
import com.saba.integration.core.service.ConnectorComponent;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class SuccessfactorComponentControl extends AbstractComponentControl {

    private static final Logger log = LoggerFactory.getLogger(SuccessfactorComponentControl.class);

    public SuccessfactorComponentControl(ConnectorComponent component) {
        super(component);
    }

    @Override
    public void beforeSend(ConnectorContext context, HttpRequest request) {
        log.info("beforeSend method called");

        // Authentication: Basic Auth
        String username = context.getConfiguration().get(SuccessfactorConstants.CONFIG_USERNAME);
        String password = context.getConfiguration().get(SuccessfactorConstants.CONFIG_PASSWORD);
        String companyId = context.getConfiguration().get(SuccessfactorConstants.CONFIG_COMPANY_ID);

        String authString = username + "@" + companyId + ":" + password;
        String encodedAuth = java.util.Base64.getEncoder().encodeToString(authString.getBytes());
        request.getHeaders().put(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);

        // Add Content-Type header
        request.getHeaders().put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

        // Add Accept header
        request.getHeaders().put(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString());

        //Add custom headers for debugging and monitoring
        String apiUrl = request.getHeaders().get(SuccessfactorConstants.HEADER_API_URL);
        if (apiUrl != null) {
            log.debug("API URL: {}", apiUrl);
        }

        String updatedFrom = request.getHeaders().get(SuccessfactorConstants.HEADER_UPDATED_FROM);
        if (updatedFrom != null) {
            log.debug("Updated From: {}", updatedFrom);
        }

         String updatedTo = request.getHeaders().get(SuccessfactorConstants.HEADER_UPDATED_TO);
        if (updatedTo != null) {
            log.debug("Updated To: {}", updatedTo);
        }

        String objectType = request.getHeaders().get(SuccessfactorConstants.HEADER_OBJECT_TYPE);
        if (objectType != null) {
            log.debug("Object Type: {}", objectType);
        }

        String integrationMonitoringId = request.getHeaders().get(SuccessfactorConstants.HEADER_INTEGRATION_MONITORING_ID);
        if (integrationMonitoringId != null) {
            log.debug("Integration Monitoring ID: {}", integrationMonitoringId);
        }

        log.info("beforeSend method completed");
    }

    @Override
    public void afterReceive(ConnectorContext context, HttpResponse response) throws IOException {
        log.info("afterReceive method called");
        log.info("afterReceive method completed");
    }

    @Override
    public Map<String, Object> buildRequest(ConnectorContext context) {
        log.info("buildRequest method called");
        Map<String, Object> requestParams = new HashMap<>();
        log.info("buildRequest method completed");
        return requestParams;
    }
}
package com.saba.integration.apps.udemy;

import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.apps.util.FlowGraphCreator;
import com.saba.integration.component.IntegrationSource;
import com.saba.integration.http.authentication.AbstractReusableAuthStrategy;
import com.saba.integration.message.Message;
import com.saba.integration.marketplace.exception.MarketplaceMessage;
import com.saba.integration.resolver.ResolverUtil;
import com.saba.integration.stream.IntegrationStreamNode;
import com.saba.integration.stream.flow.IntegrationFlow;
import com.saba.kernel.logging.SabaLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

/**
 * Udemy Integration Flows
 *
 * Contains the IntegrationFlow beans and the UdemyClientCredentialsAuthStrategy.
 * No @Bean definitions for auth strategy or component control — the JSON type
 * registry (jsonType_registry.xml) handles their instantiation automatically.
 *
 * @author Nova Integration Team
 */
@Configuration
public class UdemyFlows {

    // IntegrationFlow beans for different entity types
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

    @Bean(name = "integration.udemy.import.enrollment")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow enrollmentImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        return FlowGraphCreator.generateIntegrationFlow(
            "/com/saba/mapping/udemy/flow/Content.js",
            "integration.udemy.import.enrollment",
            sourceNodeMap, id, true);
    }

    /**
     * Udemy Client Credentials Authentication Strategy
     * Handles obtaining and caching the OAuth2 access token.
     */
    public static class UdemyClientCredentialsAuthStrategy extends AbstractReusableAuthStrategy {

        private static final SabaLogger log = SabaLogger.getLogger(UdemyClientCredentialsAuthStrategy.class);
        private final RestTemplate restTemplate = new RestTemplate();

        @Override
        public MultiValueMap<String, String> getAuthHeaders(Message message, Map<String, String> detail) {
            log.info(UdemyConstants.LOG_AUTH_PREFIX + "Generating authentication headers.");

            if (detail == null) {
                log.error(UdemyConstants.LOG_AUTH_PREFIX + "Authentication detail configuration missing.");
                throw new MarketplaceMessage(UdemyConstants.ERROR_TOKEN_GENERATION_FAILED);
            }

            String baseUrl = resolve(message, UdemyConstants.CONFIG_BASE_URL);
            String clientId = resolve(message, UdemyConstants.CONFIG_CLIENT_ID);
            String clientSecret = resolve(message, UdemyConstants.CONFIG_CLIENT_SECRET);
            String portalId = resolve(message, UdemyConstants.CONFIG_PORTAL_ID); // Not used for token, but passed from config

            if (StringUtils.isEmpty(baseUrl) || StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
                log.error(UdemyConstants.LOG_AUTH_PREFIX + "Missing required credentials (Base URL, Client ID, or Client Secret).");
                throw new MarketplaceMessage(UdemyConstants.ERROR_INVALID_CREDENTIALS);
            }

            String accessToken = generateAccessToken(baseUrl, clientId, clientSecret, portalId);

            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            if (StringUtils.isNotEmpty(accessToken)) {
                headers.add(UdemyConstants.HEADER_AUTHORIZATION, "Bearer " + accessToken);
                log.info(UdemyConstants.LOG_AUTH_PREFIX + "Bearer token generated successfully.");
            } else {
                log.error(UdemyConstants.LOG_AUTH_PREFIX + "Access token was null or empty after generation attempt.");
                throw new MarketplaceMessage(UdemyConstants.ERROR_TOKEN_GENERATION_FAILED);
            }
            return headers;
        }

        /**
         * Generates an OAuth2 access token using client credentials grant type.
         * This method is package-private static so it can be reused by TestConnection.
         *
         * @param baseUrl The base URL of the Udemy API.
         * @param clientId The client ID for authentication.
         * @param clientSecret The client secret for authentication.
         * @param portalId The portal ID (not directly used for token generation, but part of config).
         * @return The generated access token.
         * @throws MarketplaceMessage if token generation fails.
         */
        static String generateAccessToken(String baseUrl, String clientId, String clientSecret, String portalId) {
            SabaLogger staticLog = SabaLogger.getLogger(UdemyClientCredentialsAuthStrategy.class);
            staticLog.info(UdemyConstants.LOG_AUTH_PREFIX + "Attempting to generate access token.");

            // The token endpoint is directly under the base URL, not under /api-2.0/
            String tokenUrl = baseUrl + UdemyConstants.TOKEN_URL_PATH;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            // Basic Auth header for client credentials
            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.add(UdemyConstants.HEADER_AUTHORIZATION, "Basic " + encodedAuth);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", UdemyConstants.GRANT_TYPE_CLIENT_CREDENTIALS);
            body.add("scope", UdemyConstants.SCOPE_READ);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            try {
                RestTemplate staticRestTemplate = new RestTemplate(); // Use a new instance for static method
                ResponseEntity<Map> response = staticRestTemplate.exchange(tokenUrl, HttpMethod.POST, request, Map.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    String accessToken = (String) response.getBody().get(UdemyConstants.ACCESS_TOKEN);
                    if (StringUtils.isNotBlank(accessToken)) {
                        staticLog.info(UdemyConstants.LOG_AUTH_PREFIX + "Access token successfully retrieved.");
                        return accessToken;
                    } else {
                        staticLog.error(UdemyConstants.LOG_AUTH_PREFIX + "Access token not found in response body.");
                        throw new MarketplaceMessage(UdemyConstants.ERROR_TOKEN_GENERATION_FAILED);
                    }
                } else {
                    staticLog.error(UdemyConstants.LOG_AUTH_PREFIX + "Failed to get access token. Status: " + response.getStatusCode());
                    throw new MarketplaceMessage(UdemyConstants.ERROR_TOKEN_GENERATION_FAILED + ": " + response.getStatusCode());
                }
            } catch (HttpClientErrorException e) {
                staticLog.error(UdemyConstants.LOG_AUTH_PREFIX + "HTTP error during token generation: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
                if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    throw new MarketplaceMessage(UdemyConstants.ERROR_INVALID_CREDENTIALS);
                }
                throw new MarketplaceMessage(UdemyConstants.ERROR_TOKEN_GENERATION_FAILED + ": " + e.getMessage());
            } catch (Exception e) {
                staticLog.error(UdemyConstants.LOG_AUTH_PREFIX + "Exception during token generation: " + e.getMessage(), e);
                throw new MarketplaceMessage(UdemyConstants.ERROR_CONNECTION_FAILED + ": " + e.getMessage());
            }
        }
    }
}
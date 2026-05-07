package com.saba.integration.apps.stripe;

import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.apps.util.FlowGraphCreator;
import com.saba.integration.component.IntegrationSource;
import com.saba.integration.http.authentication.AbstractReusableAuthStrategy;
import com.saba.integration.message.Message;
import com.saba.integration.resolver.ResolverUtil;
import com.saba.integration.stream.IntegrationStreamNode;
import com.saba.integration.stream.flow.IntegrationFlow;
import com.saba.kernel.logging.SabaLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.Set;

@Configuration
public class StripeFlows {
    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.StripeLogger);

    // -----------------------------------------------------------------------------------------------------------------
    // IntegrationFlow Beans
    // Each @Bean defines an integration flow for a specific Stripe entity type.
    // The bean name follows the convention: "integration.{connector}.import.{entity}".
    // All flows use the same Content.js file, which handles routing based on entity type.
    // -----------------------------------------------------------------------------------------------------------------

    @Bean(name = "integration.stripe.import.customer")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow stripeCustomerImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        log.info(StripeConstants.LOG_PREFIX + "Creating customer import flow for id=" + id);
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/stripe/flow/Content.js",
                    "integration.stripe.import.customer",
                    sourceNodeMap, id, true); // 'true' indicates JSON content
        } catch (Exception e) {
            log.error(StripeConstants.LOG_PREFIX + "Failed to load Stripe customer import flow", e);
            return null;
        }
    }

    @Bean(name = "integration.stripe.import.invoice")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow stripeInvoiceImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        log.info(StripeConstants.LOG_PREFIX + "Creating invoice import flow for id=" + id);
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/stripe/flow/Content.js",
                    "integration.stripe.import.invoice",
                    sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error(StripeConstants.LOG_PREFIX + "Failed to load Stripe invoice import flow", e);
            return null;
        }
    }

    @Bean(name = "integration.stripe.import.subscription")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow stripeSubscriptionImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        log.info(StripeConstants.LOG_PREFIX + "Creating subscription import flow for id=" + id);
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/stripe/flow/Content.js",
                    "integration.stripe.import.subscription",
                    sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error(StripeConstants.LOG_PREFIX + "Failed to load Stripe subscription import flow", e);
            return null;
        }
    }

    @Bean(name = "integration.stripe.import.product")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow stripeProductImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        log.info(StripeConstants.LOG_PREFIX + "Creating product import flow for id=" + id);
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/stripe/flow/Content.js",
                    "integration.stripe.import.product",
                    sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error(StripeConstants.LOG_PREFIX + "Failed to load Stripe product import flow", e);
            return null;
        }
    }

    @Bean(name = "integration.stripe.import.price")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow stripePriceImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        log.info(StripeConstants.LOG_PREFIX + "Creating price import flow for id=" + id);
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/stripe/flow/Content.js",
                    "integration.stripe.import.price",
                    sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error(StripeConstants.LOG_PREFIX + "Failed to load Stripe price import flow", e);
            return null;
        }
    }

    @Bean(name = "integration.stripe.import.event")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow stripeEventImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        log.info(StripeConstants.LOG_PREFIX + "Creating event import flow for id=" + id);
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/stripe/flow/Content.js",
                    "integration.stripe.import.event",
                    sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error(StripeConstants.LOG_PREFIX + "Failed to load Stripe event import flow", e);
            return null;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Authentication Strategy (API Key / Bearer Token Pattern)
    // This static inner class is instantiated by the JSON type registry (jsonType_registry.xml),
    // not as a Spring @Bean. It handles the generation of the Authorization header for Stripe.
    // -----------------------------------------------------------------------------------------------------------------

    public static class StripeAuthStrategy extends AbstractReusableAuthStrategy {

        private Map<String, Object> detail;

        public void setDetail(Map<String, Object> detail) {
            this.detail = detail;
        }

        public Map<String, Object> getDetail() {
            return detail;
        }

        /**
         * Generates the authentication headers for Stripe API requests.
         * This method is called by the framework once per flow run, and its result is cached.
         * On a 401 Unauthorized response, the cache is cleared, and this method is called again.
         *
         * @param message The current integration message, used for resolving configuration details.
         * @return A MultiValueMap containing the HTTP headers, including the Authorization header.
         */
        @Override
        public MultiValueMap<String, String> getAuthHeaders(Message message) {
            log.info(StripeConstants.LOG_AUTH_PREFIX + "Generating authentication headers for Stripe API.");

            if (detail == null) {
                log.error(StripeConstants.LOG_AUTH_PREFIX + "Configuration details missing for authentication strategy.");
                return new LinkedMultiValueMap<>();
            }

            // Resolve configuration values from the 'detail' map, which comes from Content.js
            String baseUrl = resolve(message, StripeConstants.CONFIG_BASE_URL);
            String apiKey = resolve(message, StripeConstants.CONFIG_API_KEY);

            if (StringUtils.isEmpty(baseUrl)) {
                baseUrl = StripeConstants.DEFAULT_BASE_URL;
                log.warn(StripeConstants.LOG_AUTH_PREFIX + "Base URL not provided in configuration, using default: " + baseUrl);
            }

            if (StringUtils.isEmpty(apiKey)) {
                log.error(StripeConstants.LOG_AUTH_PREFIX + "Missing required API Key configuration. Cannot authenticate.");
                throw new RuntimeException(StripeConstants.ERROR_INVALID_CREDENTIALS);
            }

            // Generate the Authorization header using the shared static method
            MultiValueMap<String, String> headers = generateStripeAuthHeaders(apiKey);
            // Add the base URL to headers for potential use by Content.js or ComponentControl
            headers.add(StripeConstants.HEADER_API_URL, baseUrl);

            log.info(StripeConstants.LOG_AUTH_PREFIX + "Stripe API Key header generated successfully.");
            return headers;
        }

        /**
         * Helper method to resolve a configuration value from the 'detail' map.
         * Supports SpEL expressions for dynamic resolution.
         *
         * @param message The current message context.
         * @param key     The key of the configuration property to resolve.
         * @return The resolved string value.
         */
        private String resolve(Message message, String key) {
            Object val = detail.get(key);
            if (val == null) return null;
            return (String) ResolverUtil.resolveValue(message, (String) val, null);
        }
    }

    /**
     * Generates the Authorization header for Stripe API Key.
     * This method is package-private static so it can be reused by StripeTestConnection
     * and the StripeAuthStrategy, ensuring DRY principle for authentication logic.
     *
     * @param apiKey The Stripe API key.
     * @return A MultiValueMap containing the Authorization header.
     */
    static MultiValueMap<String, String> generateStripeAuthHeaders(String apiKey) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        if (StringUtils.isNotEmpty(apiKey)) {
            headers.add(StripeConstants.HEADER_AUTHORIZATION, StripeConstants.AUTH_SCHEME_BEARER + " " + apiKey);
        }
        return headers;
    }
}
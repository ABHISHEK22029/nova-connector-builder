package com.saba.integration.apps.udemy;

import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.apps.util.FlowGraphCreator;
import com.saba.integration.component.IntegrationSource;
import com.saba.integration.stream.IntegrationStreamNode;
import com.saba.integration.stream.flow.IntegrationFlow;
import com.saba.kernel.logging.SabaLogger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Map;
import java.util.Set;

/**
 * Udemy Integration Flows
 *
 * This class defines Spring IntegrationFlow beans for different entity types
 * (e.g., content, user).
 *
 * For Udemy, authentication (OAuth2 Client Credentials) is handled by the
 * framework's built-in OAuth2 strategy, configured within the Content.js
 * authenticationStrategy block. Therefore, no custom AuthenticationStrategy
 * inner class is required here.
 *
 * CRITICAL RULES:
 * - ONLY @Bean for IntegrationFlow — NEVER for ComponentControl or AuthStrategy.
 *   The JSON type registry (jsonType_registry.xml) handles their instantiation.
 * - Bean name format: "integration.{connector}.import.{entity}"
 * - All beans must be SCOPE_PROTOTYPE (new instance per flow execution).
 * - Flow path must match actual resource location: /com/saba/mapping/{connector}/flow/{Entity}.js
 *
 * @author Nova Integration Team
 */
@Configuration
public class UdemyFlows {
    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdemyLogger);

    /**
     * Content (Course) import flow.
     * - Bean name matches DML flow_bean_name: "integration.udemy.import.content"
     * - Path matches resource location: /com/saba/mapping/udemy/flow/Content.js
     * - SCOPE_PROTOTYPE = new instance per flow execution
     */
    @Bean(name = "integration.udemy.import.content")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow udemyContentImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/udemy/flow/Content.js",
                    "integration.udemy.import.content",
                    sourceNodeMap, id, true); // true for JSON-based flow
        } catch (Exception e) {
            log.error("Failed to load Udemy content import flow", e);
            return null;
        }
    }

    /**
     * User import flow.
     * - Bean name matches DML flow_bean_name: "integration.udemy.import.user"
     * - Path matches resource location: /com/saba/mapping/udemy/flow/User.js
     * - SCOPE_PROTOTYPE = new instance per flow execution
     */
    @Bean(name = "integration.udemy.import.user")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow udemyUserImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/udemy/flow/User.js",
                    "integration.udemy.import.user",
                    sourceNodeMap, id, true); // true for JSON-based flow
        } catch (Exception e) {
            log.error("Failed to load Udemy user import flow", e);
            return null;
        }
    }
}
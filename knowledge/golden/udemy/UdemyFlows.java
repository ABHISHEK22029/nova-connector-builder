/*
 * ============================================================================
 * GOLDEN REFERENCE: UdemyFlows.java — Basic Auth Pattern (SIMPLEST)
 * ============================================================================
 * AUTH TYPE: Basic Auth (client_id + client_secret → Base64 Authorization header)
 * 
 * KEY DIFFERENCES FROM SESSION TOKEN (Kaltura):
 * 1. NO inner auth strategy class needed — BasicAuth is built into Content.js
 * 2. Content.js authenticationStrategy uses "type": "basicAuth"
 * 3. Flows.java contains ONLY IntegrationFlow @Bean definitions
 * 4. This is the SIMPLEST pattern — most new connectors should follow this
 * 
 * PATTERN:
 * - One @Bean per entity type (content, learningPath, etc.)
 * - No auth strategy class
 * - No ComponentControl @Bean
 * ============================================================================
 */
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

@Configuration
public class UdemyFlows {
    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdemyLogger);

    /*
     * Content import flow.
     * - Bean name matches DML flow_bean_name: "integration.udemy.import.content"
     * - Path matches resource location: /com/saba/mapping/udemy/flow/Catalog.js
     * - SCOPE_PROTOTYPE = new instance per flow execution
     */
    @Bean(name = "integration.udemy.import.content")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow udemyCatalogImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/udemy/flow/Catalog.js",
                    "integration.udemy.import.content",
                    sourceNodeMap, id);
        } catch (Exception e) {
            log.error("Failed to load udemy catalog import flow", e);
            return null;
        }
    }

    /*
     * Learning path import flow — second entity type.
     * Shows how to add multiple entity flows in the same connector.
     */
    @Bean(name = "integration.udemy.import.learningPath")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow udemyLearningPathImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/udemy/flow/UdemyLearningPath.js",
                    "integration.udemy.import.learningPath",
                    sourceNodeMap, id);
        } catch (Exception e) {
            log.error("Failed to load udemy learning path import flow", e);
            return null;
        }
    }
}

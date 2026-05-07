/*
 * ============================================================================
 * GOLDEN REFERENCE: LinkedInLearningFlows.java — OAuth2 Pattern (No Auth Class)
 * ============================================================================
 * AUTH TYPE: OAuth2 Client Credentials (handled entirely in Content.js)
 * 
 * KEY DIFFERENCES FROM SESSION TOKEN (Kaltura):
 * 1. NO inner auth strategy class — OAuth2 is handled by Content.js
 *    authenticationStrategy block with "type": "oauth2v2"
 * 2. The framework has a built-in oauth2v2 strategy
 * 3. Flows.java contains ONLY IntegrationFlow @Bean definitions
 * 
 * KEY DIFFERENCES FROM BASIC AUTH (Udemy):
 * 1. Same Flows.java pattern — only @Bean for IntegrationFlow
 * 2. The difference is in Content.js: "type": "oauth2v2" vs "type": "basicAuth"
 * 3. OAuth2 requires token URL and grant type in Content.js
 * 
 * RULE: For OAuth2 connectors, Flows.java is nearly identical to Basic Auth.
 * All the auth complexity lives in Content.js.
 * ============================================================================
 */
package com.saba.integration.apps.lil;

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
import java.util.*;

@Configuration
public class LinkedInLearningFlows {
    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.LinkedInLearningLogger);

    @Bean(name = "integration.lil.import.content")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow lilCatalogImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/linkedin_learning/flow/Catalog.js",
                    "integration.lil.import.content",
                    sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error("Failed to load lil catalog import flow", e);
            return null;
        }
    }

    @Bean(name = "integration.lil.import.learningpath")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow lilLearningPathImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/linkedin_learning/flow/LearningPath.js",
                    "integration.lil.import.learningpath",
                    sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error("Failed to load lil Learning Path import flow", e);
            return null;
        }
    }
}

package com.saba.integration.apps.business;

import com.saba.integration.apps.util.FlowGraphCreator;
import com.saba.integration.component.IntegrationSource;
import com.saba.integration.stream.IntegrationStreamNode;
import com.saba.integration.stream.flow.IntegrationFlow;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Map;
import java.util.Set;

/**
 * Business Integration Flows
 *
 * Contains the IntegrationFlow beans for different entities.
 * No @Bean definitions for auth strategy or component control — the JSON type
 * registry (jsonType_registry.xml) handles their instantiation automatically.
 *
 * For API Key authentication, a custom AuthenticationStrategy class is not
 * required as the API key is directly added to headers by the framework
 * based on configuration in Content.js.
 *
 * @author Nova Integration Team
 */
@Configuration
public class BusinessFlows {

    /**
     * Defines the IntegrationFlow for importing 'content' data from Business.
     *
     * @param id The unique integration instance ID.
     * @param sourceNodeMap Map of source nodes for the flow.
     * @return An IntegrationFlow instance for content import.
     */
    @Bean(name = "integration.business.import.content")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow contentImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        return FlowGraphCreator.generateIntegrationFlow(
            "/com/saba/mapping/business/flow/Content.js",
            "integration.business.import.content",
            sourceNodeMap, id, true);
    }

    /**
     * Defines the IntegrationFlow for importing 'user' data from Business.
     *
     * @param id The unique integration instance ID.
     * @param sourceNodeMap Map of source nodes for the flow.
     * @return An IntegrationFlow instance for user import.
     */
    @Bean(name = "integration.business.import.user")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow userImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        return FlowGraphCreator.generateIntegrationFlow(
            "/com/saba/mapping/business/flow/Content.js",
            "integration.business.import.user",
            sourceNodeMap, id, true);
    }
}
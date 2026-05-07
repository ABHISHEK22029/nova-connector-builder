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

    @Bean(name = "integration.udemy.import.content")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow udemyCatalogImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/udemy/flow/Catalog.js",
                    "integration.udemy.import.content",
                    sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error("Failed to load udemy catalog import flow", e);
            return null;
        }
    }
}
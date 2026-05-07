package com.saba.integration.apps.udacity;

import com.saba.integration.framework.async.IntegrationStreamNode;
import com.saba.integration.framework.async.IntegrationSource;
import com.saba.integration.framework.flow.FlowGraphCreator;
import com.saba.integration.utils.SabaLogger;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
public class UdacityFlows {

    private static final SabaLogger log = SabaLogger.getLogger(UdacityFlows.class);

    @Bean(name = "udacity.catalog.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow udacityCatalogDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/udacity/flow/UdacityCatalog.js", "udacity.catalog.data.fetch", sourceNodeMap, id,true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for Udacity catalog data fetch" , e);
            return null;
        }
    }
}
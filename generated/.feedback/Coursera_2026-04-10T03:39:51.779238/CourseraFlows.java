package com.saba.integration.apps.coursera;

import java.util.Map;
import java.util.Set;

import com.saba.integration.framework.async.IntegrationSource;
import com.saba.integration.framework.async.IntegrationStreamNode;
import com.saba.integration.framework.flow.FlowGraphCreator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.dsl.IntegrationFlow;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class CourseraFlows {

    @Bean(name = "coursera.catalog.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow courseraCatalogDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/coursera/flow/CourseraCatalog.js", "coursera.catalog.data.fetch", sourceNodeMap, id,true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for coursera catalog data fetch" , e);
            return null;
        }
    }

    @Bean(name = "coursera.user.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow courseraUserDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/coursera/flow/CourseraUser.js", "coursera.user.data.fetch", sourceNodeMap, id,true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for coursera user data fetch" , e);
            return null;
        }
    }
}
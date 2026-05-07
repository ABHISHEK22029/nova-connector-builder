package com.saba.integration.apps.relly;

import com.saba.integration.apps.commons.base.FlowGraphCreator;
import com.saba.integration.apps.commons.model.IntegrationStreamNode;
import com.saba.integration.apps.commons.model.enums.IntegrationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.dsl.IntegrationFlow;

import java.util.Map;
import java.util.Set;

@Configuration
public class RellyFlows {

    private static final Logger log = LoggerFactory.getLogger(RellyFlows.class);

    @Bean(name = "relly.employee.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow rellyEmployeeDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/relly/flow/RellyEmployee.js", "relly.employee.data.fetch", sourceNodeMap, id,true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for relly employee data fetch" , e);
            return null;
        }
    }
}
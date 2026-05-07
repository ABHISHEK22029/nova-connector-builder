package com.saba.integration.apps.hrms;

import com.saba.integration.apps.commons.base.IntegrationStreamNode;
import com.saba.integration.apps.commons.base.IntegrationSource;
import com.saba.integration.apps.commons.creator.FlowGraphCreator;
import com.saba.integration.apps.commons.logging.SabaLogger;
import com.saba.integration.apps.commons.apps.AppsLogger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.dsl.IntegrationFlow;

import java.util.Map;
import java.util.Set;

@Configuration
public class HrmsFlows {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.HrmsLogger);

    @Bean(name = "hrms.employee.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow hrmsEmployeeDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/hrms/flow/HrmsEmployee.js", "hrms.employee.data.fetch", sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for hrms employee data fetch", e);
            return null;
        }
    }

    @Bean(name = "hrms.department.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow hrmsDepartmentDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/hrms/flow/HrmsDepartment.js", "hrms.department.data.fetch", sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for hrms department data fetch", e);
            return null;
        }
    }
}
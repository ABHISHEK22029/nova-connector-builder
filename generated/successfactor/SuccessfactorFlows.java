package com.saba.integration.apps.successfactor;

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
public class SuccessfactorFlows {
    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.SuccessfactorLogger);

    @Bean(name = "integration.successfactor.import.user")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow successfactorUserImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow(
                    "/com/saba/mapping/successfactor/flow/Content.js",
                    "integration.successfactor.import.user",
                    sourceNodeMap, id, true);
        } catch (Exception e) {
            log.error("Failed to load successfactor user import flow", e);
            return null;
        }
    }
}
```
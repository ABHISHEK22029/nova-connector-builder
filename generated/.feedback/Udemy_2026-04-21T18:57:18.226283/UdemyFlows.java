package com.saba.integration.apps.udemy;

import com.saba.integration.core.api.FlowGraphCreator;
import com.saba.integration.core.model.IntegrationStreamNode;
import com.saba.integration.core.model.IntegrationSource;
import com.saba.integration.core.util.AppsLogger;
import com.saba.integration.core.util.SabaLogger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.dsl.IntegrationFlow;

import java.util.Map;
import java.util.Set;

@Configuration
public class UdemyFlows {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdemyLogger);

    @Bean(name = "udemy.course.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow udemyCourseDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/udemy/flow/UdemyCourse.js", "udemy.course.data.fetch", sourceNodeMap, id,true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for udemy course data fetch" , e);
            return null;
        }
    }

    @Bean(name = "udemy.user.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow udemyUserDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/udemy/flow/UdemyUser.js", "udemy.user.data.fetch", sourceNodeMap, id,true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for udemy user data fetch" , e);
            return null;
        }
    }

    @Bean(name = "udemy.review.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow udemyReviewDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/udemy/flow/UdemyReview.js", "udemy.review.data.fetch", sourceNodeMap, id,true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for udemy review data fetch" , e);
            return null;
        }
    }
}
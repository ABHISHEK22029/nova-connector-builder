package com.saba.integration.apps.zoom;

import com.saba.integration.core.model.IntegrationStreamNode;
import com.saba.integration.core.source.IntegrationSource;
import com.saba.integration.core.flow.FlowGraphCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.dsl.IntegrationFlow;

import java.util.Map;
import java.util.Set;

@Configuration
@Slf4j
public class ZoomFlows {

    @Bean(name = "zoom.meeting.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow zoomMeetingDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/zoom/flow/ZoomMeeting.js", "zoom.meeting.data.fetch", sourceNodeMap, id,true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for zoom meeting data fetch" , e);
            return null;
        }
    }

    @Bean(name = "zoom.webinar.data.fetch")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow zoomWebinarDataFlow(String id, Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        try {
            return FlowGraphCreator.generateIntegrationFlow("/com/saba/mapping/zoom/flow/ZoomWebinar.js", "zoom.webinar.data.fetch", sourceNodeMap, id,true);
        } catch (Exception e) {
            log.error("Failed to create flow graph for zoom webinar data fetch" , e);
            return null;
        }
    }
}
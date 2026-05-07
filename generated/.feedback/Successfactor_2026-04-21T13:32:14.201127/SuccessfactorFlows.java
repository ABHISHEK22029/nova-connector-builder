package com.saba.integration.apps.successfactor;

import com.saba.integration.apps.successfactor.auth.SuccessfactorAuthenticationStrategy;
import com.saba.integration.core.flow.AbstractFlows;
import com.saba.integration.core.flow.FlowDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Configuration
public class SuccessfactorFlows extends AbstractFlows {

    @Autowired
    private SuccessfactorAuthenticationStrategy authenticationStrategy;

    @Bean
    public List<FlowDefinition> flowDefinitions() {
        return Arrays.asList(
                FlowDefinition.builder()
                        .id("integ-6e91794a-9f19-451f-b953-c993a7c17c9a")
                        .name("Get Users")
                        .description("Retrieves users from SuccessFactors.")
                        .objectType("User")
                        .operationType("READ")
                        .authenticationStrategy(authenticationStrategy)
                        .componentId("com.saba.integration.apps.successfactor.components.UserReader")
                        .build(),
                FlowDefinition.builder()
                        .id("integ-4b7b5f0a-9c2a-4b5a-a8e3-7f9d1b2a3e4c")
                        .name("Get Learning Activities")
                        .description("Retrieves learning activities from SuccessFactors.")
                        .objectType("LearningActivity")
                        .operationType("READ")
                        .authenticationStrategy(authenticationStrategy)
                        .componentId("com.saba.integration.apps.successfactor.components.LearningActivityReader")
                        .build()
        );
    }

    @Override
    protected List<FlowDefinition> getFlowDefinitions() {
        return flowDefinitions();
    }
}
package com.saba.integration.apps.udemy;

import com.saba.integration.apps.logging.AppsLogger;
import com.saba.integration.apps.util.FlowGraphCreator;
import com.saba.integration.component.IntegrationSource;
import com.saba.integration.http.authentication.AbstractReusableAuthStrategy;
import com.saba.integration.message.Message;
import com.saba.integration.resolver.ResolverUtil;
import com.saba.integration.stream.IntegrationStreamNode;
import com.saba.integration.stream.flow.IntegrationFlow;
import com.saba.kernel.logging.SabaLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;

@Configuration
public class UdemyFlows {

    private static final SabaLogger log = new AppsLogger(UdemyFlows.class);

    @Bean(name = "integration.udemy.import.content")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow contentImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        return FlowGraphCreator.generateIntegrationFlow(
            "/com/saba/mapping/udemy/flow/Content.js",
            "integration.udemy.import.content",
            sourceNodeMap, id, true);
    }

    @Bean(name = "integration.udemy.import.learningpath")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow learningPathImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        return FlowGraphCreator.generateIntegrationFlow(
            "/com/saba/mapping/udemy/flow/LearningPath.js",
            "integration.udemy.import.learningpath",
            sourceNodeMap, id, true);
    }

    @Bean(name = "integration.udemy.import.user")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IntegrationFlow userImportFlow(String id,
            Map<String, Set<IntegrationStreamNode<IntegrationSource>>> sourceNodeMap) {
        return FlowGraphCreator.generateIntegrationFlow(
            "/com/saba/mapping/udemy/flow/User.js",
            "integration.udemy.import.user",
            sourceNodeMap, id, true);
    }

    public static class UdemyAuthStrategy extends AbstractReusableAuthStrategy {

        public static MultiValueMap<String, String> generateAuthHeaders(Message message) {
            log.info("Generating authentication headers");

            String clientId = ResolverUtil.resolve(message, "clientId");
            String clientSecret = ResolverUtil.resolve(message, "clientSecret");

            if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)) {
                log.error("Missing required config");
                throw new RuntimeException("Missing Udemy Configuration");
            }

            String encoded = java.util.Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add("Authorization", "Basic " + encoded);
            return headers;
        }

        @Override
        public MultiValueMap<String, String> getAuthHeaders(Message message) {
            return generateAuthHeaders(message);
        }
    }
}
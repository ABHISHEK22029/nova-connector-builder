package com.cornerstoneondemand.connector.udemy;

import com.cornerstoneondemand.connector.udemy.auth.UdemyAuthentication;
import com.saba.spi.connectors.flow.Flows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;

@Configuration
public class UdemyFlows implements Flows {

  private final UdemyAuthentication authentication;

  public UdemyFlows(UdemyAuthentication authentication) {
    this.authentication = authentication;
  }

  @Bean
  public IntegrationFlow authenticationFlow() {
    return authentication.authenticationFlow();
  }
}
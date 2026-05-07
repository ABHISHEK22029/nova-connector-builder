package com.cornerstoneondemand.connector.udemy;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.saba.spi.connectors.flow.AbstractComponentControl;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdemyComponentControl extends AbstractComponentControl {

  private static final Logger log = LoggerFactory.getLogger(UdemyComponentControl.class);

  @Override
  public void beforeExecute(
      HttpRequest request, String objectType, String operation, Map<String, Object> configuration)
      throws IOException {
    super.beforeExecute(request, objectType, operation, configuration);

    HttpHeaders headers = request.getHeaders();
    if (headers == null) {
      headers = new HttpHeaders();
    }

    // Add authentication header
    String clientId = (String) configuration.get(UdemyConstants.CLIENT_ID);
    String clientSecret = (String) configuration.get(UdemyConstants.CLIENT_SECRET);
    String authString = clientId + ":" + clientSecret;
    String authStringEnc = java.util.Base64.getEncoder().encodeToString(authString.getBytes());
    headers.set(UdemyConstants.AUTHORIZATION_HEADER, "Basic " + authStringEnc);

    request.setHeaders(headers);
  }

  @Override
  public void afterExecute(
      HttpRequest request, String objectType, String operation, Map<String, Object> configuration)
      throws IOException {
    super.afterExecute(request, objectType, operation, configuration);
  }
}
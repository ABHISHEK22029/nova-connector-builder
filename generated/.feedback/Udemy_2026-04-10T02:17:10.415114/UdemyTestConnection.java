package com.cornerstoneondemand.connector.udemy;

import com.cornerstoneondemand.connector.udemy.auth.UdemyAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saba.spi.connectors.flow.AbstractTestConnection;
import com.saba.spi.connectors.types.TestConnectionResponse;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdemyTestConnection extends AbstractTestConnection {

  private static final Logger log = LoggerFactory.getLogger(UdemyTestConnection.class);

  @Override
  public TestConnectionResponse testConnection(Map<String, Object> configuration) {
    try {
      String baseUrl = (String) configuration.get(UdemyConstants.BASE_URL);
      String clientId = (String) configuration.get(UdemyConstants.CLIENT_ID);
      String clientSecret = (String) configuration.get(UdemyConstants.CLIENT_SECRET);

      NetHttpTransport transport = new NetHttpTransport();
      HttpRequestFactory requestFactory = transport.createRequestFactory();

      GenericUrl url = new GenericUrl(baseUrl + UdemyConstants.COURSES_ENDPOINT);
      HttpRequest request = requestFactory.buildGetRequest(url);

      // Set Authentication Header
      UdemyAuthentication.setAuthenticationHeader(request, clientId, clientSecret);

      HttpResponse response = request.execute();
      String content = response.parseAsString();

      // Check for a successful response (200 OK)
      if (response.getStatusCode() == 200) {
        // Attempt to parse the JSON response to further validate the connection
        try {
          JsonObject jsonResponse = JsonParser.parseString(content).getAsJsonObject();
          if (jsonResponse != null) {
            return TestConnectionResponse.success();
          } else {
            log.error("Failed to parse JSON response from Udemy. Response: {}", content);
            return TestConnectionResponse.failure("Failed to parse JSON response from Udemy.");
          }
        } catch (Exception e) {
          log.error("Error parsing JSON response: ", e);
          return TestConnectionResponse.failure("Error parsing JSON response: " + e.getMessage());
        }
      } else {
        log.error(
            "Test connection failed with status code: {} and response: {}",
            response.getStatusCode(),
            content);
        return TestConnectionResponse.failure(
            "Test connection failed with status code: "
                + response.getStatusCode()
                + " and response: "
                + content);
      }

    } catch (Exception e) {
      log.error("Test connection failed: ", e);
      return TestConnectionResponse.failure("Test connection failed: " + e.getMessage());
    }
  }
}
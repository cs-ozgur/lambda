package com.digitalsanctum.lambda.server.util;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * @author Shane Witbeck
 * @since 3/3/17
 */
public class RESTUtils {

  public static boolean isEndpointAvailable(CloseableHttpClient httpClient, String endpoint, int sleepPeriod) {
    try {
      HttpGet healthCheck = new HttpGet(endpoint);
      CloseableHttpResponse response = httpClient.execute(healthCheck);
      if (response.getStatusLine().getStatusCode() == 200) {
        return true;
      }
    } catch (Exception e) {
      try {
        Thread.sleep(sleepPeriod);
      } catch (InterruptedException e1) {
        // noop
      }
    }
    return false;
  }
}

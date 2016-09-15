package com.digitalsanctum.lambda.server.service.resource;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Shane Witbeck
 * @since 7/25/16
 */
public class HealthcheckTest extends LocalBaseTest {

  @Test
  public void testHealthcheck() throws Exception {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet request = new HttpGet(LAMBDA_SERVER_ENDPOINT + "/healthcheck");
    HttpResponse response = httpClient.execute(request);
    assertEquals(200, response.getStatusLine().getStatusCode());
  }
}

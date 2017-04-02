package com.digitalsanctum.lambda.server.resource;

import com.digitalsanctum.lambda.lifecycle.AWSLocal;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * @author Shane Witbeck
 * @since 7/25/16
 */
public class HealthcheckResourceTest {
  
  private static final Logger log = LoggerFactory.getLogger(HealthcheckResourceTest.class);

  private static final String LAMBDA_SERVER_ENDPOINT = "http://localhost:8080";

  private static AWSLocal awsLocal;

  @BeforeClass
  public static void before() throws Exception {

    awsLocal = AWSLocal.builder()
        .enableLambda(AWSLocal.LambdaServiceType.FILESYSTEM)
        .build();
    awsLocal.start();

    log.info("setup complete");
  }

  @AfterClass
  public static void after() throws Exception {
    awsLocal.stop();
  }

  @Before
  public void setup() throws Exception {
   
  }

  @Test
  public void testHealthcheck() throws Exception {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet request = new HttpGet(LAMBDA_SERVER_ENDPOINT + "/healthcheck");
    HttpResponse response = httpClient.execute(request);
    assertEquals(200, response.getStatusLine().getStatusCode());
  }
}

package com.digitalsanctum.lambda.server.service.resource;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.digitalsanctum.lambda.server.LambdaServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Shane Witbeck
 * @since 8/5/16
 */
public class LocalBaseTest extends BaseTest {

  private static final int TEST_PORT = 8080;
  protected static final String ENDPOINT = "http://localhost:" + TEST_PORT;
  private static LambdaServer lambdaServer;

  @BeforeClass
  public static void before() throws Exception {
    lambdaServer = new LambdaServer(TEST_PORT);
    lambdaServer.start();
  }

  @AfterClass
  public static void after() throws Exception {
    lambdaServer.stop();
  }
  
  @Before
  public void setup() throws Exception {
    awsLambda = new AWSLambdaClient().withEndpoint(ENDPOINT);
  }
}

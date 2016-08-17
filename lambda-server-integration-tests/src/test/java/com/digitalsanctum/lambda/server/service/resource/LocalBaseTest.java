package com.digitalsanctum.lambda.server.service.resource;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.digitalsanctum.lambda.bridge.server.DockerBridgeServer;
import com.digitalsanctum.lambda.server.LambdaServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shane Witbeck
 * @since 8/5/16
 */
public class LocalBaseTest extends BaseTest {
  
  private static final Logger log = LoggerFactory.getLogger(LocalBaseTest.class);

  private static final int LAMBDA_SERVER_PORT = 8080;
  protected static final String ENDPOINT = "http://localhost:" + LAMBDA_SERVER_PORT;
  
  private static LambdaServer lambdaServer;
  private static DockerBridgeServer dockerBridgeServer;

  @BeforeClass
  public static void before() throws Exception {
    dockerBridgeServer = new DockerBridgeServer(8082);
    dockerBridgeServer.start();
    
    lambdaServer = new LambdaServer(LAMBDA_SERVER_PORT);
    lambdaServer.start();
  }

  @AfterClass
  public static void after() throws Exception {
    if (lambdaServer.isRunning()) {
      log.info("stopping LambdaServer");
      lambdaServer.stop();
    }    
    if (dockerBridgeServer.isRunning()) {
      log.info("stopping DockerBridgeServer");
      dockerBridgeServer.stop();
    }
  }
  
  @Before
  public void setup() throws Exception {
    awsLambda = new AWSLambdaClient().withEndpoint(ENDPOINT);
  }
}

package com.digitalsanctum.lambda.server.service.resource;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.digitalsanctum.lambda.imagebuilder.server.DockerBridgeServer;
import com.digitalsanctum.lambda.server.LambdaServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Shane Witbeck
 * @since 8/5/16
 */
public class LocalBaseTest extends BaseTest {

  private static final int DOCKER_BRIDGE_SERVER_PORT = 8082;
  private static final int LAMBDA_SERVER_PORT = 8080;
  protected static final String ENDPOINT = "http://localhost:" + LAMBDA_SERVER_PORT;
  
  private static DockerBridgeServer dockerBridgeServer;
  private static LambdaServer lambdaServer;

  @BeforeClass
  public static void before() throws Exception {

    dockerBridgeServer = new DockerBridgeServer(DOCKER_BRIDGE_SERVER_PORT);
    dockerBridgeServer.start();
    
    lambdaServer = new LambdaServer(LAMBDA_SERVER_PORT);
    lambdaServer.start();
  }

  @AfterClass
  public static void after() throws Exception {
    if (lambdaServer != null) {
      lambdaServer.stop();
    }
    if (dockerBridgeServer != null) {
      dockerBridgeServer.stop();
    }
  }
  
  @Before
  public void setup() throws Exception {
    awsLambda = new AWSLambdaClient().withEndpoint(ENDPOINT);
  }
}

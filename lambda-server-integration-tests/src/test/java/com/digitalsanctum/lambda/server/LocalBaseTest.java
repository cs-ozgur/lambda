package com.digitalsanctum.lambda.server;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;
import com.digitalsanctum.dynamodb.DockerDynamoDB;
import com.digitalsanctum.kinesis.DockerKinesis;
import com.digitalsanctum.lambda.bridge.server.DockerBridgeServer;
import com.digitalsanctum.lambda.kinesispoller.kinesis.processor.KclWorker;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 8/5/16
 */
public class LocalBaseTest extends BaseTest {
  
  private static final Logger log = LoggerFactory.getLogger(LocalBaseTest.class);

  private static final int BRIDGE_SERVER_PORT = 8082;
  private static final int LAMBDA_SERVER_PORT = 8080;  
  
  private static LambdaServer lambdaServer;
  private static DockerBridgeServer dockerBridgeServer;
  private static DockerDynamoDB dockerDynamoDB;
  private static DockerKinesis dockerKinesis;
  
  protected static final String LAMBDA_SERVER_ENDPOINT = "http://localhost:" + LAMBDA_SERVER_PORT;
  protected static final String TEST_STREAM = "test-stream";
  protected static AmazonKinesisClient amazonKinesisClient;
  protected static KclWorker kclWorker;

  @BeforeClass
  public static void before() throws Exception {

    dockerDynamoDB = new DockerDynamoDB();
    int dynamoDbPort = dockerDynamoDB.start();
    String dynamoDbEndpoint = "http://localhost:" + dynamoDbPort;

    dockerKinesis = new DockerKinesis();
    int kinesisPort = dockerKinesis.start();
    String kinesisEndpoint = "http://localhost:" + kinesisPort;

    // Kinesalite does not support CBOR
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
    assertThat(SDKGlobalConfiguration.isCborDisabled(), is(true));

    log.info("instantiating Kinesis client with endpoint: {}", kinesisEndpoint);
    amazonKinesisClient = new AmazonKinesisClient();
    amazonKinesisClient.setEndpoint(kinesisEndpoint);

    log.info("creating {} Kinesis stream with shard count of {}", TEST_STREAM, 1);
    amazonKinesisClient.createStream(TEST_STREAM, 1);

    Waiter<DescribeStreamRequest> waiter = amazonKinesisClient.waiters().streamExists();
    waiter.run(new WaiterParameters<>(new DescribeStreamRequest().withStreamName(TEST_STREAM)));

    DescribeStreamResult describeStreamResult = amazonKinesisClient.describeStream(TEST_STREAM);
    assertThat(describeStreamResult.getStreamDescription().getStreamName(), is(TEST_STREAM));

    log.info("starting KCL worker");
    kclWorker = new KclWorker(TEST_STREAM, kinesisEndpoint, dynamoDbEndpoint, LAMBDA_SERVER_ENDPOINT, new DefaultAWSCredentialsProviderChain());
    kclWorker.start();

    // make sure to kill containers
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        log.info("shutting down containers from shutdown hook");
        dockerKinesis.stop();
        dockerDynamoDB.stop();
      }
    });
    
    dockerBridgeServer = new DockerBridgeServer(BRIDGE_SERVER_PORT);
    dockerBridgeServer.start();
    
    lambdaServer = new LambdaServer(LAMBDA_SERVER_PORT);
    lambdaServer.start();
       
    log.info("setup complete");
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

    log.info("stopping KCL worker");
    kclWorker.stop();

    log.info("stopping Kinesis container");
    dockerKinesis.stop();
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "");

    log.info("stopping DynamoDB container");
    dockerDynamoDB.stop();
  }
  
  @Before
  public void setup() throws Exception {
    AwsClientBuilder.EndpointConfiguration endpointConfiguration 
        = new AwsClientBuilder.EndpointConfiguration(LAMBDA_SERVER_ENDPOINT, "local");
    awsLambda = AWSLambdaClientBuilder.standard().withEndpointConfiguration(endpointConfiguration).build();
  }
}

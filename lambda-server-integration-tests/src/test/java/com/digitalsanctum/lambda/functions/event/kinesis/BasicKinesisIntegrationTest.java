package com.digitalsanctum.lambda.functions.event.kinesis;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreamsClient;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.util.IOUtils;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;
import com.digitalsanctum.lambda.kinesispoller.kinesis.processor.KclWorker;
import com.digitalsanctum.lambda.kinesispoller.kinesis.processor.LambdaInvokingProcessorFactory;
import com.digitalsanctum.lambda.server.AWSLocal;
import com.digitalsanctum.lambda.server.resource.FunctionResourceTest;
import com.digitalsanctum.lambda.service.localfile.LocalFileSystemService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;

import static com.amazonaws.services.lambda.model.EventSourcePosition.TRIM_HORIZON;
import static com.digitalsanctum.lambda.service.localfile.LocalFileEventSourceMappingService.MAPPING_SUFFIX;
import static com.digitalsanctum.lambda.service.localfile.LocalFileEventSourceMappingService.ROOT_DIR;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Tests that a lambda function can successfully consume a Kinesis stream.
 *
 * @author Shane Witbeck
 * @since 3/3/17
 */
public class BasicKinesisIntegrationTest {

  private static final Logger log = LoggerFactory.getLogger(BasicKinesisIntegrationTest.class);

  private static final String FUNCTION_NAME = "basic-kinesis";

  private static final String TEST_FUNCTION_JAR = "/test-functions/lambda.jar";
  private static final String FUNCTION_RUNTIME = "java8";
  private static final String FUNCTION_HANDLER = "com.digitalsanctum.lambda.functions.event.kinesis.BasicKinesis::handler";
  private static final String FUNCTION_ARN = "arn:aws:lambda:local:111000111000:function:" + FUNCTION_NAME;

  private static final int LAMBDA_SERVER_PORT = 8080;

  protected static final String LAMBDA_SERVER_ENDPOINT = "http://localhost:" + LAMBDA_SERVER_PORT;
  protected static final String TEST_STREAM = "test-stream";
  protected static KclWorker kclWorker;
  protected AWSLambda awsLambda;

  private static AWSLocal awsLocal;

  private LocalFileSystemService localFileSystemService;

  private AmazonDynamoDB amazonDynamoDB;
  private AmazonKinesis amazonKinesis;

  @BeforeClass
  public static void before() throws Exception {

    awsLocal = AWSLocal.builder()
        .enableDynamoDB()
        .enableKinesisStreams()
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

    // instantiate DynamoDB client to point to local DynamoDB
    String dynamoDbEndpoint = awsLocal.getDynamoDbEndpoint();
    AwsClientBuilder.EndpointConfiguration dynamoDbEndpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration(dynamoDbEndpoint, "local");
    amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(dynamoDbEndpointConfiguration)
        .build();

    // Kinesalite does not support CBOR
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
    assertThat(SDKGlobalConfiguration.isCborDisabled(), is(true));

    String kinesisEndpoint = awsLocal.getKinesisEndpoint();
    log.info("instantiating Kinesis client with endpoint: {}", kinesisEndpoint);
    AwsClientBuilder.EndpointConfiguration kinesisEndpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration(kinesisEndpoint, awsLocal.getSigningRegion());
    amazonKinesis = AmazonKinesisClientBuilder.standard().withEndpointConfiguration(kinesisEndpointConfiguration).build();

    AwsClientBuilder.EndpointConfiguration endpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration(LAMBDA_SERVER_ENDPOINT, awsLocal.getSigningRegion());
    awsLambda = AWSLambdaClientBuilder.standard().withEndpointConfiguration(endpointConfiguration).build();

    localFileSystemService = new LocalFileSystemService();
    deleteAllEventSourceMappings();
  }

  @After
  public void afterTest() throws Exception {
    deleteAllEventSourceMappings();
  }

  @Test
  public void testEndToEndKinesisStreamConsumedByLambdaFunction() throws Exception {

    // create function code from local test jar 
    InputStream is = FunctionResourceTest.class.getResourceAsStream(TEST_FUNCTION_JAR);
    byte[] lambdaByteArr = IOUtils.toByteArray(is);
    ByteBuffer functionZip = ByteBuffer.wrap(lambdaByteArr);
    FunctionCode functionCode = new FunctionCode().withZipFile(functionZip);

    // create function
    CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
        .withFunctionName(FUNCTION_NAME) // must be lowercase since we're using this as the docker repository name
        .withCode(functionCode)
        .withRuntime(FUNCTION_RUNTIME)
        .withPublish(true)
        .withHandler(FUNCTION_HANDLER);

    CreateFunctionResult createFunctionResult = awsLambda.createFunction(createFunctionRequest);
    assertNotNull(createFunctionResult);
    assertEquals(FUNCTION_NAME, createFunctionResult.getFunctionName());
    assertEquals(FUNCTION_ARN, createFunctionResult.getFunctionArn());

    // create Kinesis stream
    log.info("creating {} Kinesis stream with shard count functionArn {}", TEST_STREAM, 1);
    amazonKinesis.createStream(TEST_STREAM, 1);
    
    // wait for stream to become available
    Waiter<com.amazonaws.services.kinesis.model.DescribeStreamRequest> waiter = amazonKinesis.waiters().streamExists();
    waiter.run(new WaiterParameters<>(new com.amazonaws.services.kinesis.model.DescribeStreamRequest().withStreamName(TEST_STREAM)));
    
    // verify stream exists and get the stream ARN
    com.amazonaws.services.kinesis.model.DescribeStreamResult describeStreamResult = amazonKinesis.describeStream(TEST_STREAM);
    assertThat(describeStreamResult.getStreamDescription().getStreamName(), is(TEST_STREAM));
    String streamARN = describeStreamResult.getStreamDescription().getStreamARN();

    // create EventSourceMapping
    CreateEventSourceMappingRequest createEventSourceMappingRequest = new CreateEventSourceMappingRequest()
        .withEnabled(true)
        .withFunctionName(createFunctionResult.getFunctionName())
        .withStartingPosition(TRIM_HORIZON)
        .withEventSourceArn(streamARN)
        .withBatchSize(100);


    CreateEventSourceMappingResult createEventSourceMappingResult = awsLambda.createEventSourceMapping(createEventSourceMappingRequest);
    log.info(createEventSourceMappingResult.toString());
    
    // TODO inject EventSourceMapping to kinesis poller (KCL)
    
    // TODO start the poller
/*
    log.info("starting KCL worker");
    new LambdaInvokingProcessorFactory()
    kclWorker = new KclWorker(TEST_STREAM, streamARN, awsLocal.getKinesisEndpoint(), awsLocal.getDynamoDbEndpoint(), LAMBDA_SERVER_ENDPOINT, FUNCTION_NAME,  new DefaultAWSCredentialsProviderChain());
    kclWorker.start();

    log.info("waiting 30s to allow worker to initialize");
    Thread.sleep(30_000);
*/
    
    // TODO put test record on Kinsesis stream
    
    // TODO poller should consume test record and invoke Lambda with KinesisEvent


  }


  private void deleteAllEventSourceMappings() throws Exception {
    PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + ROOT_DIR + "**/*" + MAPPING_SUFFIX);
    try {
      Files.walk(ROOT_DIR)
          .filter(pathMatcher::matches)
          .forEach(path -> localFileSystemService.delete(path));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  
}

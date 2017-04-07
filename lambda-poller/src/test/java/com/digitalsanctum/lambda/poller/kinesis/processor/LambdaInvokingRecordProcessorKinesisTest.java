package com.digitalsanctum.lambda.poller.kinesis.processor;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.model.CreateStreamResult;
import com.amazonaws.services.kinesis.model.DescribeStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
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
import com.digitalsanctum.lambda.lifecycle.AWSLocal;
import com.digitalsanctum.lambda.poller.kinesis.KclWorker;
import com.digitalsanctum.lambda.server.util.ArnUtils;
import com.google.common.base.Charsets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.amazonaws.services.lambda.model.EventSourcePosition.TRIM_HORIZON;
import static com.digitalsanctum.lambda.lifecycle.AWSLocal.LambdaServiceType.FILESYSTEM;
import static com.digitalsanctum.lambda.lifecycle.AWSLocal.SIGNING_REGION;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 4/2/17
 */
public class LambdaInvokingRecordProcessorKinesisTest {

  private static final Logger log = LoggerFactory.getLogger(LambdaInvokingRecordProcessorKinesisTest.class);

  private static final String TEST_STREAM = "test-stream";
  private static final String LAMBDA_SERVER_ENDPOINT = "http://localhost:8080";
  private static final String FUNCTION_NAME = "basickinesis";
  private static final String FUNCTION_HANDLER = "com.digitalsanctum.lambda.functions.event.kinesis.BasicKinesis::handler";
  static final String TEST_LAMBDA_JAR = "/test-functions/lambda.jar";
  static final String TEST_RUNTIME = "java8";

  private static AWSLocal awsLocal;
  private static AmazonKinesis amazonKinesis;
  private static KclWorker kclWorker;
  private static AWSLambda awsLambda;

  @BeforeClass
  public static void setupClazz() throws Exception {

    awsLocal = AWSLocal.builder(FILESYSTEM)
        .enableDynamoDB()
        .enableKinesisStreams()
        .build();
    awsLocal.start();

    String dynamoDbEndpoint = awsLocal.getDynamoDbEndpoint();
    String kinesisEndpoint = awsLocal.getKinesisEndpoint();


    AwsClientBuilder.EndpointConfiguration lambdaEndpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration(LAMBDA_SERVER_ENDPOINT, awsLocal.getSigningRegion());
    awsLambda = AWSLambdaClientBuilder.standard().withEndpointConfiguration(lambdaEndpointConfiguration).build();

    // Kinesalite does not support CBOR
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
    assertThat(SDKGlobalConfiguration.isCborDisabled(), is(true));

    log.info("instantiating Kinesis client with endpoint: {}", kinesisEndpoint);
    AwsClientBuilder.EndpointConfiguration kinesisEndpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration(kinesisEndpoint, SIGNING_REGION);
    amazonKinesis = AmazonKinesisClient.builder().withEndpointConfiguration(kinesisEndpointConfiguration).build();

    log.info("creating {} Kinesis stream with shard count of {}", TEST_STREAM, 1);
    CreateStreamResult createStreamResult = amazonKinesis.createStream(TEST_STREAM, 1);
    String kinesisStreamArn = ArnUtils.kinesisStreamArn(TEST_STREAM);
    

    Waiter<DescribeStreamRequest> waiter = amazonKinesis.waiters().streamExists();
    waiter.run(new WaiterParameters<>(new DescribeStreamRequest().withStreamName(TEST_STREAM)));

    DescribeStreamResult describeStreamResult = amazonKinesis.describeStream(TEST_STREAM);
    assertThat(describeStreamResult.getStreamDescription().getStreamName(), is(TEST_STREAM));

    // create function code from local test jar 
    InputStream is = LambdaInvokingRecordProcessorKinesisTest.class.getResourceAsStream(TEST_LAMBDA_JAR);
    byte[] lambdaByteArr = IOUtils.toByteArray(is);
    ByteBuffer functionZip = ByteBuffer.wrap(lambdaByteArr);
    FunctionCode functionCode = new FunctionCode().withZipFile(functionZip);

    // create function
    CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
        .withFunctionName(FUNCTION_NAME) // must be lowercase since we're using this as the docker repository name
        .withCode(functionCode)
        .withRuntime(TEST_RUNTIME)
        .withPublish(true)
        .withHandler(FUNCTION_HANDLER);

    CreateFunctionResult createFunctionResult = awsLambda.createFunction(createFunctionRequest);
    assertNotNull(createFunctionResult);
    assertEquals(FUNCTION_NAME, createFunctionResult.getFunctionName());    
    assertEquals(ArnUtils.functionArn(FUNCTION_NAME), createFunctionResult.getFunctionArn());

    // create mapping
    CreateEventSourceMappingRequest createEventSourceMappingRequest = new CreateEventSourceMappingRequest()
        .withEnabled(true)
        .withFunctionName(createFunctionResult.getFunctionName())
        .withStartingPosition(TRIM_HORIZON)
        .withBatchSize(100)
        .withEventSourceArn(kinesisStreamArn);
    
    // TODO for each EventSourceMapping, create a new KclWorker (many lambdas consuming from one stream)

    CreateEventSourceMappingResult createEventSourceMappingResult
        = awsLambda.createEventSourceMapping(createEventSourceMappingRequest);
    log.info(createEventSourceMappingResult.toString());

    IRecordProcessorFactory recordProcessorFactory
        = new LambdaInvokingProcessorFactory(lambdaEndpointConfiguration, FUNCTION_NAME);

    log.info("starting KCL worker");
    AWSCredentialsProvider awsCredentialsProvider = new DefaultAWSCredentialsProviderChain();
    kclWorker = new KclWorker(TEST_STREAM, kinesisEndpoint, dynamoDbEndpoint, awsCredentialsProvider, recordProcessorFactory);
    kclWorker.start();
    
    // TODO determine why it takes so long for initialization

    log.info("waiting 30s to allow worker to initialize");
    Thread.sleep(30_000);


    log.info("setup complete");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    awsLocal.stop();
  }

  @Test
  public void testBasicProducerConsumer() throws Exception {

    log.info("workerId={}", kclWorker.getWorkerId());

    int index = 1;

    while (index < 10) {
      String testRecord = "Test Record " + index;
      ByteBuffer data = ByteBuffer.wrap(testRecord.getBytes(Charsets.UTF_8));

      PutRecordRequest putRecordRequest = new PutRecordRequest()
          .withPartitionKey("TestPartitionKey")
          .withStreamName(TEST_STREAM)
          .withData(data);

      log.info(">>> sending: {}", testRecord);
      amazonKinesis.putRecord(putRecordRequest);

      Thread.sleep(1000);
      index++;
    }
  }
}

package com.digitalsanctum.lambda.server;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.DescribeStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;
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
public abstract class LocalBaseTest {
  
  private static final Logger log = LoggerFactory.getLogger(LocalBaseTest.class);

  private static final int LAMBDA_SERVER_PORT = 8080;  
  
  protected static final String LAMBDA_SERVER_ENDPOINT = "http://localhost:" + LAMBDA_SERVER_PORT;
  protected static final String TEST_STREAM = "test-stream";
  protected static AmazonKinesis amazonKinesis;
  protected static KclWorker kclWorker;
  
  protected AWSLambda awsLambda;
  
  protected static AWSLocal awsLocal;

  @BeforeClass
  public static void before() throws Exception {
    
    awsLocal = AWSLocal.builder()
        .enableDynamoDB()
        .enableLambda(AWSLocal.LambdaServiceType.FILESYSTEM)
        .build();
    awsLocal.start();
        
    if (awsLocal.isKinesisStreamsEnabled()) {
      String kinesisEndpoint = awsLocal.getKinesisEndpoint();
      log.info("instantiating Kinesis client with endpoint: {}", kinesisEndpoint);
      // Kinesalite does not support CBOR
      System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
      assertThat(SDKGlobalConfiguration.isCborDisabled(), is(true));
      AwsClientBuilder.EndpointConfiguration endpointConfiguration
          = new AwsClientBuilder.EndpointConfiguration(kinesisEndpoint, awsLocal.getSigningRegion());
      amazonKinesis = AmazonKinesisClientBuilder.standard().withEndpointConfiguration(endpointConfiguration).build();

      log.info("creating {} Kinesis stream with shard count functionArn {}", TEST_STREAM, 1);
      amazonKinesis.createStream(TEST_STREAM, 1);
      Waiter<DescribeStreamRequest> waiter = amazonKinesis.waiters().streamExists();
      waiter.run(new WaiterParameters<>(new DescribeStreamRequest().withStreamName(TEST_STREAM)));
      DescribeStreamResult describeStreamResult = amazonKinesis.describeStream(TEST_STREAM);
      assertThat(describeStreamResult.getStreamDescription().getStreamName(), is(TEST_STREAM));
    }

    log.info("setup complete");
  }

  @AfterClass
  public static void after() throws Exception {
    awsLocal.stop();
  }
  
  @Before
  public void setup() throws Exception {
    AwsClientBuilder.EndpointConfiguration endpointConfiguration 
        = new AwsClientBuilder.EndpointConfiguration(LAMBDA_SERVER_ENDPOINT, awsLocal.getSigningRegion());
    awsLambda = AWSLambdaClientBuilder.standard().withEndpointConfiguration(endpointConfiguration).build();
  }
}

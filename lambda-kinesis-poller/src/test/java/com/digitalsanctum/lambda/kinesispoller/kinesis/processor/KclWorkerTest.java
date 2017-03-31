package com.digitalsanctum.lambda.kinesispoller.kinesis.processor;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamRequest;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.waiters.Waiter;
import com.amazonaws.waiters.WaiterParameters;
import com.digitalsanctum.dynamodb.DockerDynamoDB;
import com.digitalsanctum.kinesis.DockerKinesis;
import com.google.common.base.Charsets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 8/19/16
 */
public class KclWorkerTest {

  private static final Logger log = LoggerFactory.getLogger(KclWorkerTest.class);

  private static final String TEST_STREAM = "test-stream";

  private static DockerDynamoDB dockerDynamoDB;
  private static AmazonKinesis amazonKinesis;
  private static DockerKinesis dockerKinesis;
  private static KclWorker kclWorker;

  @BeforeClass
  public static void setupClazz() throws Exception {

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
    AwsClientBuilder.EndpointConfiguration endpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration(kinesisEndpoint, "local");
    amazonKinesis = AmazonKinesisClient.builder().withEndpointConfiguration(endpointConfiguration).build();

    log.info("creating {} Kinesis stream with shard count of {}", TEST_STREAM, 1);
    amazonKinesis.createStream(TEST_STREAM, 1);

    Waiter<DescribeStreamRequest> waiter = amazonKinesis.waiters().streamExists();
    waiter.run(new WaiterParameters<>(new DescribeStreamRequest().withStreamName(TEST_STREAM)));

    DescribeStreamResult describeStreamResult = amazonKinesis.describeStream(TEST_STREAM);
    assertThat(describeStreamResult.getStreamDescription().getStreamName(), is(TEST_STREAM));

    log.info("starting KCL worker");
    kclWorker = new KclWorker(TEST_STREAM, kinesisEndpoint, dynamoDbEndpoint, null, new DefaultAWSCredentialsProviderChain());
    kclWorker.start();

    // make sure to kill containers
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("shutting down containers from shutdown hook");
      dockerKinesis.stop();
      dockerDynamoDB.stop();
    }));

    log.info("setup complete");
  }

  @AfterClass
  public static void tearDown() throws Exception {

    log.info("stopping KCL worker");
    kclWorker.stop();

    log.info("stopping Kinesis container");
    dockerKinesis.stop();
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "");

    log.info("stopping DynamoDB container");
    dockerDynamoDB.stop();
  }

  @Test
  public void testFoo() throws Exception {

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
      Thread.sleep(2000);
      index++;
    }
  }

}

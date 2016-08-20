package com.digitalsanctum.lambda.kinesispoller.kinesis.processor;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.digitalsanctum.dynamodb.DockerDynamoDB;
import com.digitalsanctum.kinesis.DockerKinesis;
import com.google.common.base.Charsets;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
  private static AmazonKinesisClient amazonKinesisClient;
  private static DockerKinesis dockerKinesis;
  private static KclWorker kclWorker;

  @BeforeClass
  public static void setupClazz() throws Exception {

    log.info("starting DynamoDB container");
    dockerDynamoDB = new DockerDynamoDB();
    int dynamoDbPort = dockerDynamoDB.start();
    String dynamoDbEndpoint = "http://localhost:" + dynamoDbPort;

    log.info("starting Kinesis container");
    dockerKinesis = new DockerKinesis();
    int kinesisPort = dockerKinesis.start();
    String kinesisEndpoint = "http://localhost:" + kinesisPort;

    // make sure to kill containers
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        log.info("shutting down containers from shutdown hook");
        dockerKinesis.stop();
        dockerDynamoDB.stop();
      }
    });

    // Kinesalite does not support CBOR
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
    assertThat(SDKGlobalConfiguration.isCborDisabled(), is(true));

    log.info("instantiating Kinesis client with endpoint: {}", kinesisEndpoint);
    amazonKinesisClient = new AmazonKinesisClient();
    amazonKinesisClient.setEndpoint(kinesisEndpoint);

    log.info("creating Kinesis stream");
    amazonKinesisClient.createStream(TEST_STREAM, 1);
    DescribeStreamResult describeStreamResult = amazonKinesisClient.describeStream(TEST_STREAM);
    assertThat(describeStreamResult.getStreamDescription().getStreamName(), is(TEST_STREAM));

    log.info("starting KCL worker");
    kclWorker = new KclWorker(TEST_STREAM, kinesisEndpoint, dynamoDbEndpoint, new DefaultAWSCredentialsProviderChain());
    kclWorker.start();

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
  @Ignore
  public void testFoo() throws Exception {

    Thread.sleep(5000); // wait for stream to become available TODO use waiter
    log.info("workerId={}", kclWorker.getWorkerId());


    int index = 1;

    while (true) {

      String testRecord = "Test Record " + index;
      ByteBuffer data = ByteBuffer.wrap(testRecord.getBytes(Charsets.UTF_8));

      PutRecordRequest putRecordRequest = new PutRecordRequest()
          .withPartitionKey("TestPartitionKey")
          .withStreamName(TEST_STREAM)
          .withData(data);

      log.info(">>> sending: {}", testRecord);
      amazonKinesisClient.putRecord(putRecordRequest);
      Thread.sleep(2000);
      index++;
    }

  }

}

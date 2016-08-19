package com.digitalsanctum.kinesis;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 8/19/16
 */
public class DockerKinesisTest {

  private static final String TEST_STREAM = "test";

  private static AmazonKinesisClient amazonKinesisClient;
  private static DockerKinesis dockerKinesis;

  @BeforeClass
  public static void setupClazz() throws Exception {
    dockerKinesis = new DockerKinesis();
    int port = dockerKinesis.start();

    // Kinesalite does not support CBOR
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
    assertThat(SDKGlobalConfiguration.isCborDisabled(), is(true));
    
    amazonKinesisClient = new AmazonKinesisClient();
    amazonKinesisClient.setEndpoint("http://localhost:" + port);
  }

  @AfterClass
  public static void tearDown() throws Exception {
    dockerKinesis.stop();
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "");
  }

  @Test
  public void testCreateStream() throws Exception {
    amazonKinesisClient.createStream(TEST_STREAM, 1);
    
    DescribeStreamResult describeStreamResult = amazonKinesisClient.describeStream(TEST_STREAM);

    assertThat(describeStreamResult.getStreamDescription().getStreamName(), is(TEST_STREAM));
  }
}

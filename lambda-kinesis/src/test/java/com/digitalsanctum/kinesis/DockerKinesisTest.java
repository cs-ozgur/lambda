package com.digitalsanctum.kinesis;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
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

  private static AmazonKinesis amazonKinesis;
  private static DockerKinesis dockerKinesis;

  @BeforeClass
  public static void setupClazz() throws Exception {
    dockerKinesis = new DockerKinesis();
    int port = dockerKinesis.start();
    
    // Kinesalite does not support CBOR
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "true");
    assertThat(SDKGlobalConfiguration.isCborDisabled(), is(true));

    AwsClientBuilder.EndpointConfiguration endpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration("http://localhost:" + port, "local");

    amazonKinesis = AmazonKinesisClient.builder().withEndpointConfiguration(endpointConfiguration).build();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    dockerKinesis.stop();
    System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "");
  }

  @Test
  public void testCreateStream() throws Exception {
    amazonKinesis.createStream(TEST_STREAM, 1);

    DescribeStreamResult describeStreamResult = amazonKinesis.describeStream(TEST_STREAM);

    assertThat(describeStreamResult.getStreamDescription().getStreamName(), is(TEST_STREAM));
  }
}

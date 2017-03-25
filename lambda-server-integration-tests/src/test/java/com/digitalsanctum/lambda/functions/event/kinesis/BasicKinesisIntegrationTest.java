package com.digitalsanctum.lambda.functions.event.kinesis;

import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.util.IOUtils;
import com.digitalsanctum.lambda.server.LocalBaseTest;
import com.digitalsanctum.lambda.server.resource.FunctionResourceTest;
import com.google.common.base.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests that a lambda function can successfully consume a Kinesis stream.
 * 
 * @author Shane Witbeck
 * @since 3/3/17
 */
public class BasicKinesisIntegrationTest extends LocalBaseTest {

  private static final Logger log = LoggerFactory.getLogger(BasicKinesisIntegrationTest.class);

  private static final String TEST_KINESIS_FUNCTION_NAME = "basic-kinesis";

  private static final String TEST_LAMBDA_JAR = "/test-functions/lambda.jar";
  private static final String TEST_RUNTIME = "java8";
  private static final String TEST_HANDLER = "com.digitalsanctum.lambda.functions.event.kinesis.BasicKinesis::handler";
  private static final String TEST_ARN = "arn:aws:lambda:local:111000111000:function:" + TEST_KINESIS_FUNCTION_NAME;
  private static final int TEST_TIMEOUT = 30;



  @Before
  public void setup() throws Exception {
    super.setup();
    log.info("setup");
  }

  @After
  public void tearDown() throws Exception {
    log.info("tearDown");
  }

  @Test
  @Ignore("finish me")  
  public void testRoundTrip() throws Exception {

    log.info("testRoundTrip");

    createFunction();

    // TODO setup event source mapping
    // TODO send events to kinesis stream

    int index = 1;
    while (index < 20) {
      String testRecord = "Test Record " + index;
      ByteBuffer data = ByteBuffer.wrap(testRecord.getBytes(Charsets.UTF_8));

      PutRecordRequest putRecordRequest = new PutRecordRequest()
          .withPartitionKey("TestPartitionKey")
          .withStreamName(TEST_STREAM)
          .withData(data);

      log.info(">>> sending: {}", testRecord);
      amazonKinesis.putRecord(putRecordRequest);
      Thread.sleep(100);
      index++;
    }

    // TODO watch for receiving functionArn events in lambda

  }
  
  /*private void createEventSourceMapping() throws Exception {
    CreateEventSourceMappingRequest createEventSourceMappingRequest = new CreateEventSourceMappingRequest()
        .withEventSourceArn()
        .withBatchSize(10)
        .withEnabled(Boolean.TRUE)
        .withFunctionName(TEST_KINESIS_FUNCTION_NAME);
  }*/

  private void createFunction() throws Exception {

    CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
        .withFunctionName(TEST_KINESIS_FUNCTION_NAME)
        .withHandler(TEST_HANDLER)
        .withTimeout(TEST_TIMEOUT)
        .withRuntime(TEST_RUNTIME);

    InputStream is = FunctionResourceTest.class.getResourceAsStream(TEST_LAMBDA_JAR);
    byte[] lambdaByteArr = IOUtils.toByteArray(is);
    ByteBuffer byteBuffer = ByteBuffer.wrap(lambdaByteArr);

    FunctionCode code = new FunctionCode().withZipFile(byteBuffer);
    createFunctionRequest.setCode(code);

    CreateFunctionResult result = awsLambda.createFunction(createFunctionRequest);
    assertNotNull(result);
    assertEquals(TEST_KINESIS_FUNCTION_NAME, result.getFunctionName());
    assertEquals(TEST_ARN, result.getFunctionArn());
  }
}

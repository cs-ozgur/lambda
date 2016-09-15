package com.digitalsanctum.lambda.server.service.resource;

import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.util.IOUtils;
import com.google.common.base.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Shane Witbeck
 * @since 9/10/16
 */
public class EventSourceMappingRoundTripTest extends LocalBaseTest {
  
  private static final Logger log = LoggerFactory.getLogger(EventSourceMappingRoundTripTest.class);

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
  public void testRoundTrip() throws Exception {
    
    log.info("testRoundTrip");
    
    // todo setup lambda   
    createFunction(); 
    
    
    // todo setup event source mapping
    // todo send events to kinesis stream

    log.info("workerId={}", kclWorker.getWorkerId());

    int index = 1;

    while (index < 20) {
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
    
    // todo watch for receiving of events in lambda
    
  }

  private void createFunction() throws Exception {

    CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
        .withFunctionName(TEST_KINESIS_FUNCTION_NAME)
        .withHandler(TEST_HANDLER)
        .withTimeout(TEST_TIMEOUT)
        .withRuntime(TEST_RUNTIME);

    InputStream is = LocalFunctionTest.class.getResourceAsStream(TEST_LAMBDA_JAR);
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

package com.digitalsanctum.lambda.server.resource;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.DeleteFunctionRequest;
import com.amazonaws.services.lambda.model.DeleteFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.GetFunctionRequest;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ListFunctionsRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.util.IOUtils;
import com.digitalsanctum.lambda.functions.model.ConcatRequest;
import com.digitalsanctum.lambda.lifecycle.AWSLocal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Shane Witbeck
 * @since 7/17/16
 */
public class FunctionResourceTest {

  private static final Logger log = LoggerFactory.getLogger(FunctionResourceTest.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  private static final String LAMBDA_SERVER_ENDPOINT = "http://localhost:8080";

  private static final String TEST_FUNCTION_NAME = "concat";
  static final String TEST_LAMBDA_JAR = "/test-functions/lambda.jar";
  static final String TEST_RUNTIME = "java8";
  private static final String TEST_HANDLER = "com.digitalsanctum.lambda.functions.requestresponse.Concat";
  private static final String TEST_ARN = "arn:aws:lambda:local:111000111000:function:" + TEST_FUNCTION_NAME;
  private static final int TEST_TIMEOUT = 30;

  private static AWSLocal awsLocal;
  private AWSLambda awsLambda;

  @BeforeClass
  public static void before() throws Exception {

    awsLocal = AWSLocal.builder()
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
    AwsClientBuilder.EndpointConfiguration endpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration(LAMBDA_SERVER_ENDPOINT, awsLocal.getSigningRegion());
    awsLambda = AWSLambdaClientBuilder.standard().withEndpointConfiguration(endpointConfiguration).build();
  }

  @Test
  public void testHealthcheck() throws Exception {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet request = new HttpGet(LAMBDA_SERVER_ENDPOINT + "/healthcheck"); 
    HttpResponse response = httpClient.execute(request);
    assertEquals(200, response.getStatusLine().getStatusCode());
  }

  @Test
  public void testDeleteFunctionRequest() throws Exception {

    createFunction();

    GetFunctionRequest getFunctionRequest = new GetFunctionRequest().withFunctionName(TEST_FUNCTION_NAME);
    GetFunctionResult getFunctionResult = awsLambda.getFunction(getFunctionRequest);
    assertNotNull(getFunctionResult);
    assertNotNull(getFunctionResult.getConfiguration());
    assertEquals(TEST_FUNCTION_NAME, getFunctionResult.getConfiguration().getFunctionName());
    assertEquals(TEST_RUNTIME, getFunctionResult.getConfiguration().getRuntime());

    DeleteFunctionRequest deleteFunctionRequest = new DeleteFunctionRequest();
    deleteFunctionRequest.setFunctionName(TEST_FUNCTION_NAME);
    DeleteFunctionResult deleteFunctionResult = awsLambda.deleteFunction(deleteFunctionRequest);
    assertNotNull(deleteFunctionResult);
  }

  @Test(expected = ResourceNotFoundException.class)
  public void testDeleteFunctionRequestNotFound() throws Exception {

    DeleteFunctionRequest req = new DeleteFunctionRequest();
    req.setFunctionName(TEST_FUNCTION_NAME);
    DeleteFunctionResult result = awsLambda.deleteFunction(req);
    assertNotNull(result);    
    
    DeleteFunctionRequest deleteFunctionRequest = new DeleteFunctionRequest();
    deleteFunctionRequest.setFunctionName(TEST_FUNCTION_NAME);
    DeleteFunctionResult deleteFunctionResult = awsLambda.deleteFunction(deleteFunctionRequest);
    assertNotNull(deleteFunctionResult);
  }

  @Test
  public void testListFunctionsRequest() throws Exception {
    ListFunctionsRequest listFunctionsRequest = new ListFunctionsRequest().withMaxItems(10);
    ListFunctionsResult listFunctionsResult = awsLambda.listFunctions(listFunctionsRequest);
    assertNotNull(listFunctionsResult);
  }

  @Test
  public void testCreateFunctionRequest() throws Exception {
 
    CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
        .withFunctionName(TEST_FUNCTION_NAME)
        .withHandler(TEST_HANDLER)
        .withRuntime(TEST_RUNTIME);

    InputStream is = FunctionResourceTest.class.getResourceAsStream(TEST_LAMBDA_JAR);
    byte[] lambdaByteArr = IOUtils.toByteArray(is);
    ByteBuffer byteBuffer = ByteBuffer.wrap(lambdaByteArr);
        
    FunctionCode code = new FunctionCode().withZipFile(byteBuffer);
    createFunctionRequest.setCode(code);

    CreateFunctionResult result = awsLambda.createFunction(createFunctionRequest);
    assertNotNull(result);
    assertEquals(TEST_FUNCTION_NAME, result.getFunctionName());    
    assertEquals(TEST_ARN, result.getFunctionArn());
    

    GetFunctionRequest getFunctionRequest = new GetFunctionRequest().withFunctionName(TEST_FUNCTION_NAME);
    GetFunctionResult getFunctionResult = awsLambda.getFunction(getFunctionRequest);
    assertNotNull(getFunctionResult);
    assertNotNull(getFunctionResult.getConfiguration());
    assertEquals(TEST_FUNCTION_NAME, getFunctionResult.getConfiguration().getFunctionName());
    assertEquals(TEST_RUNTIME, getFunctionResult.getConfiguration().getRuntime());
  }
  
  @Test
  public void testUpdateFunctionCodeRequest() throws Exception {
    createFunction();

    String testRequestJson = getTestRequest();

    Map<String, String> payloadMap = new HashMap<>();
    payloadMap.put("Payload", testRequestJson);
    payloadMap.put("FunctionName", TEST_FUNCTION_NAME);

    byte[] payload = mapper.writeValueAsString(payloadMap).getBytes();

    InvokeRequest invokeRequest = new InvokeRequest();
    invokeRequest.setPayload(new String(payload));
    invokeRequest.setFunctionName(TEST_FUNCTION_NAME);

    InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
    
    ByteBuffer byteBuffer = invokeResult.getPayload();
    ByteBuffer decodedResultPayload = Base64.getDecoder().decode(byteBuffer);
    String resultPayloadJson = new String(decodedResultPayload.array(), Charsets.UTF_8);
    System.out.println(resultPayloadJson);
    
    UpdateFunctionCodeRequest updateFunctionCodeRequest = new UpdateFunctionCodeRequest();
    updateFunctionCodeRequest.setPublish(true);    
  }

  /**
   * http://docs.aws.amazon.com/lambda/latest/dg/API_Invoke.html
   */
  @Test
  public void testInvokeRequest_RequestResponse() throws Exception {    
    createFunction();    
    invoke_RequestResponse();
  }
  
  private void createFunction() throws Exception {

    CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
        .withFunctionName(TEST_FUNCTION_NAME)
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
    assertEquals(TEST_FUNCTION_NAME, result.getFunctionName());
    assertEquals(TEST_ARN, result.getFunctionArn());
  }

  private void invoke_RequestResponse() throws JsonProcessingException {
    String testRequestJson = getTestRequest();

    InvokeRequest invokeRequest = new InvokeRequest();
    invokeRequest.setPayload(testRequestJson);
    invokeRequest.setFunctionName(TEST_FUNCTION_NAME);

    InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
    ByteBuffer byteBuffer = invokeResult.getPayload();
    String resultPayloadJson = new String(byteBuffer.array());
    assertEquals("{\"message\":\"Shane Witbeck\"}", resultPayloadJson);
  }

  private String getTestRequest() throws JsonProcessingException {
    ConcatRequest testRequest = new ConcatRequest();
    testRequest.setFirstName("Shane");
    testRequest.setLastName("Witbeck");
    return new String(mapper.writeValueAsBytes(testRequest));
  }
}

package com.digitalsanctum.lambda.server.service.resource;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
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
import com.amazonaws.services.lambda.model.PublishVersionRequest;
import com.amazonaws.services.lambda.model.PublishVersionResult;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * NOTE:
 * 1. update /etc/hosts to point 'lambda.local.amazonaws.com' to 127.0.0.1
 * 2. update ~/.aws/credentials and ~/.aws/config to match arg passed to ProfileCredentialsProvider
 *
 * @author Shane Witbeck
 * @since 7/17/16
 */
public class LocalFunctionTest extends LocalBaseTest {

  private static final Logger log = LoggerFactory.getLogger(LocalFunctionTest.class);

  private static final ObjectMapper mapper = new ObjectMapper();
  
  private static final String TEST_RUNTIME = "java8";
  private static final String TEST_HANDLER = "com.digitalsanctum.lambda.samples.HelloPojo";
  private static final String TEST_JAR_PATH = "/Users/switbe/projects/lambda/lambda-server-integration-tests/src/test/resources/test-functions/lambda.jar";
  private static final String TEST_ARN = "arn:aws:lambda:local:111000111000:function:" + TEST_FUNCTION_NAME;
  private static final int TEST_TIMEOUT = 30;

  @Test
  public void testHealthcheck() throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
    HttpGet request = new HttpGet(ENDPOINT + "/healthcheck"); 
    HttpResponse response = httpClient.execute(request);
    assertEquals(200, response.getStatusLine().getStatusCode());
  }
  
  @Test
  public void testPublishVersionRequest() throws Exception {

    CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
        .withFunctionName(TEST_FUNCTION_NAME)
        .withHandler(TEST_HANDLER)
        .withRuntime(TEST_RUNTIME);

    CreateFunctionResult result = awsLambda.createFunction(createFunctionRequest);
    assertNotNull(result);
    assertEquals(TEST_FUNCTION_NAME, result.getFunctionName());
    assertEquals(TEST_ARN, result.getFunctionArn());
    
    PublishVersionRequest r = new PublishVersionRequest();
    r.setFunctionName(TEST_FUNCTION_NAME);
    r.setCodeSha256("testSha");
    r.setDescription("test description");
    
    // TODO
    
    PublishVersionResult publishVersionResult = awsLambda.publishVersion(r);
    
    assertNotNull(publishVersionResult);
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

    Path lambdaPath = Paths.get(TEST_JAR_PATH);
    InputStream is = Files.newInputStream(lambdaPath);
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
  
  @Test
  @Ignore
  public void testCreateEventSourceMappingRequest() throws Exception {
    CreateEventSourceMappingRequest createEventSourceMappingRequest = new CreateEventSourceMappingRequest();
    CreateEventSourceMappingResult mappingResult = awsLambda.createEventSourceMapping(createEventSourceMappingRequest);
  }
  
  private void createFunction() throws Exception {

    CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
        .withFunctionName(TEST_FUNCTION_NAME)
        .withHandler(TEST_HANDLER)
        .withTimeout(TEST_TIMEOUT)
        .withRuntime(TEST_RUNTIME);

    Path lambdaPath = Paths.get(TEST_JAR_PATH);
    InputStream is = Files.newInputStream(lambdaPath);
    byte[] lambdaByteArr = IOUtils.toByteArray(is);
    ByteBuffer byteBuffer = ByteBuffer.wrap(lambdaByteArr);

    FunctionCode code = new FunctionCode().withZipFile(byteBuffer);
    createFunctionRequest.setCode(code);

    CreateFunctionResult result = awsLambda.createFunction(createFunctionRequest);
    assertNotNull(result);
    assertEquals(TEST_FUNCTION_NAME, result.getFunctionName());
    assertEquals(TEST_ARN, result.getFunctionArn());
  }

  
}

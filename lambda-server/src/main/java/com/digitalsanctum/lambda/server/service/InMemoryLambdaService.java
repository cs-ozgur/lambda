package com.digitalsanctum.lambda.server.service;

import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.FunctionCodeLocation;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeResult;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationResult;
import com.digitalsanctum.lambda.Definition;
import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResult;
import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResult;
import com.digitalsanctum.lambda.server.util.ArnUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 7/19/16
 */
public class InMemoryLambdaService implements LambdaService {

  private static final Logger log = LoggerFactory.getLogger(InMemoryLambdaService.class);

  private static final Map<String, FunctionConfiguration> FUNCTIONS = new HashMap<>();

  // TODO for now, we're just going to key off of FunctionName. AWS supports FunctionName and ARN
  private static final Map<String, FunctionCode> FUNCTION_CODE = new HashMap<>();

  private static final Map<String, FunctionCodeLocation> FUNCTION_CODE_LOCATION = new HashMap<>();

  private final ObjectMapper objectMapper;

  public InMemoryLambdaService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public GetFunctionResult getFunction(String functionName) {
    FunctionConfiguration functionConfiguration = FUNCTIONS.get(functionName);
    if (functionConfiguration == null) {
      return null;
    }

    GetFunctionResult result = new GetFunctionResult();
    result.setConfiguration(functionConfiguration);

    FunctionCodeLocation codeLocation = FUNCTION_CODE_LOCATION.get(functionName);
    result.setCode(codeLocation);

    return result;
  }

  @Override
  public CreateFunctionResult saveFunctionConfiguration(FunctionConfiguration f) {
    FUNCTIONS.put(f.getFunctionName(), f);

    return new CreateFunctionResult()
        .withFunctionName(f.getFunctionName())
        .withFunctionArn(f.getFunctionArn())
        .withHandler(f.getHandler());
  }

  @Override
  public FunctionConfiguration deleteFunction(String id) {
    return FUNCTIONS.remove(id);
  }

  @Override
  public ListFunctionsResult listFunctions() {
    ListFunctionsResult listFunctionsResult = new ListFunctionsResult();
    listFunctionsResult.setFunctions(new ArrayList<>(FUNCTIONS.values()));
    return listFunctionsResult;
  }

  @Override
  public Object invokeFunction(InvokeRequest invokeRequest,
                               FunctionConfiguration functionConfiguration,
                               FunctionCodeLocation functionCodeLocation) {

    Definition def = new Definition(functionConfiguration.getHandler(), 30);
    def.setName(invokeRequest.getFunctionName());

    // TODO handling of location based on repository type. for now, assume it's a local file path 
    String location = functionCodeLocation.getLocation();
    
    
    // TODO verify a container already exists before creating a new one for each invocation
    
    RunContainerRequest runContainerRequest = new RunContainerRequest();
    runContainerRequest.setImageId(location);
    runContainerRequest.setHandler(functionConfiguration.getHandler());

    RunContainerResult runContainerResult = null;
    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {

      // TODO make endpoint configurable
      HttpPost post = new HttpPost("http://localhost:8082/containers");

      String requestJson = objectMapper.writeValueAsString(runContainerRequest);
      StringEntity input = new StringEntity(requestJson);
      post.setEntity(input);
      input.setContentType("application/json");

      CloseableHttpResponse response = httpClient.execute(post);
      
      HttpEntity entity = response.getEntity();
      String responseJson = EntityUtils.toString(entity, Charsets.UTF_8);
      runContainerResult = objectMapper.readValue(responseJson.getBytes(), RunContainerResult.class);
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    // call the endpoint
    String endpoint = runContainerResult.getEndpoint();

    boolean up = false;
    log.info("waiting for container to become available");
    long start = System.currentTimeMillis();
    while(!up) {
      up = isEndpointAvailable(httpClient, endpoint, 500);
    }
    log.info("Container is available; took {} ms", (System.currentTimeMillis() - start));

    try {
      ByteBuffer payloadByteBuffer = invokeRequest.getPayload();
      String payload = new String(payloadByteBuffer.array());

      HttpPost post = new HttpPost(endpoint);
      StringEntity input = new StringEntity(payload);
      post.setEntity(input);
      input.setContentType("application/json");

      CloseableHttpResponse response = httpClient.execute(post);

      HttpEntity entity = response.getEntity();
      String responseJson = EntityUtils.toString(entity, Charsets.UTF_8);
      
      return responseJson;

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  private boolean isEndpointAvailable(CloseableHttpClient httpclient, String endpoint, int sleepPeriod) {
    try {
      HttpGet healthCheck = new HttpGet(endpoint);
      CloseableHttpResponse response = httpclient.execute(healthCheck);
      if (response.getStatusLine().getStatusCode() == 200) {
        return true;
      }
    } catch (Exception e) {
      try {
        Thread.sleep(sleepPeriod);
      } catch (InterruptedException e1) {
        // noop
      }
    }
    return false;
  }
  

  @Override
  public UpdateFunctionConfigurationResult updateFunctionConfiguration(UpdateFunctionConfigurationRequest request) {
    FunctionConfiguration fc = new FunctionConfiguration();
    fc.setFunctionName(request.getFunctionName());
    fc.setFunctionArn(ArnUtils.of(request.getFunctionName()));
    fc.setHandler(request.getHandler());
    fc.setRuntime(request.getRuntime());
    fc.setDescription(request.getDescription());
    fc.setMemorySize(request.getMemorySize());

    FunctionConfiguration savedConfig = FUNCTIONS.put(request.getFunctionName(), fc);

    UpdateFunctionConfigurationResult result = new UpdateFunctionConfigurationResult();
    result.setMemorySize(savedConfig.getMemorySize());
    result.setRuntime(savedConfig.getRuntime());
    result.setDescription(savedConfig.getDescription());
    result.setHandler(savedConfig.getHandler());
    result.setFunctionArn(savedConfig.getFunctionArn());
    result.setFunctionName(savedConfig.getFunctionName());

    return result;
  }

  @Override
  public UpdateFunctionCodeResult updateFunctionCode(UpdateFunctionCodeRequest updateFunctionCodeRequest) {
    ByteBuffer byteBuffer = updateFunctionCodeRequest.getZipFile();

    UpdateFunctionCodeResult result = new UpdateFunctionCodeResult();
    result.setFunctionName(updateFunctionCodeRequest.getFunctionName());
    result.setFunctionArn(ArnUtils.of(updateFunctionCodeRequest.getFunctionName()));

    // TODO more setters  

    CloseableHttpClient httpclient = HttpClients.createDefault();
    
    // TODO make endpoint configurable
    HttpPost post = new HttpPost("http://localhost:" + 8082 + "/images");
    
    try {

      CreateImageRequest request = new CreateImageRequest();
      request.setImageName(updateFunctionCodeRequest.getFunctionName());
      request.setLambdaJar(byteBuffer);

      String requestJson = objectMapper.writeValueAsString(request);

      StringEntity input = new StringEntity(requestJson);
      post.setEntity(input);

      input.setContentType("application/json");

      CloseableHttpResponse response = httpclient.execute(post);
      
      HttpEntity entity = response.getEntity();

      String responseJson = EntityUtils.toString(entity, Charsets.UTF_8);
      CreateImageResult createImageResult = objectMapper.readValue(responseJson.getBytes(), CreateImageResult.class);

      FunctionCode code = new FunctionCode();
      code.setZipFile(updateFunctionCodeRequest.getZipFile());
      FUNCTION_CODE.put(updateFunctionCodeRequest.getFunctionName(), code);

      FunctionCodeLocation functionCodeLocation = new FunctionCodeLocation();
      functionCodeLocation.setLocation(createImageResult.getImageId());
      functionCodeLocation.setRepositoryType("Docker");
      FUNCTION_CODE_LOCATION.put(updateFunctionCodeRequest.getFunctionName(), functionCodeLocation);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }
}

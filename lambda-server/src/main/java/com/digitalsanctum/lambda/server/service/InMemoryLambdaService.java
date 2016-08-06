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
import com.digitalsanctum.lambda.Executor;
import com.digitalsanctum.lambda.ResultProvider;
import com.digitalsanctum.lambda.server.util.ArnUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
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
  public Object invokeFunction(InvokeRequest invokeRequest, FunctionConfiguration functionConfiguration,
                               FunctionCodeLocation functionCodeLocation) {
    
    Definition def = new Definition(functionConfiguration.getHandler(), 30);
    def.setName(invokeRequest.getFunctionName());
    
    // TODO handling of location based on repository type. for now, assume it's a local file path 
    String path = functionCodeLocation.getLocation();
    
    File lambdaJar = new File(path);
    if (!lambdaJar.exists()) {
      throw new RuntimeException("Lambda jar not found: " + lambdaJar.getAbsolutePath());
    }
    
    try {
      addLambdaJar(lambdaJar);
    } catch (Exception e) {
      e.printStackTrace();
    }

    Executor executor = new Executor(def);
    try {
      ByteBuffer payloadByteBuffer = invokeRequest.getPayload();
      String payload = new String(payloadByteBuffer.array());
      
      ResultProvider resultProvider = executor.execute(payload);      
      Object resultObject = resultProvider.getResult();
      
      return objectMapper.writeValueAsString(resultObject);
      
    } catch (Exception e) {      
      e.printStackTrace();
    }
        
    return null;
  }

  private static void addLambdaJar(File file) throws Exception {
    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL());
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
    
    try {
      File tmpLambdaFile = File.createTempFile("lambda-" + System.currentTimeMillis(), ".jar", new File("/tmp"));      
      WritableByteChannel channel = Channels.newChannel(new FileOutputStream(tmpLambdaFile));
      int bytesWritten = channel.write(byteBuffer);
      log.info("wrote {} bytes to {} for function {}", 
          bytesWritten, tmpLambdaFile.getAbsolutePath(), updateFunctionCodeRequest.getFunctionName());
      
      FunctionCode code = new FunctionCode();      
      code.setZipFile(updateFunctionCodeRequest.getZipFile());      
      FUNCTION_CODE.put(updateFunctionCodeRequest.getFunctionName(), code);
      
      FunctionCodeLocation functionCodeLocation = new FunctionCodeLocation();
      functionCodeLocation.setLocation(tmpLambdaFile.getAbsolutePath());
      functionCodeLocation.setRepositoryType("local");
      FUNCTION_CODE_LOCATION.put(updateFunctionCodeRequest.getFunctionName(), functionCodeLocation);

    } catch (IOException e) {
      e.printStackTrace();
    }    
    return result;
  }
}

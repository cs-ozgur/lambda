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
import com.digitalsanctum.lambda.server.util.ArnUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.Files.delete;

/**
 * @author Shane Witbeck
 * @since 10/23/16
 */
public class LocalFileLambdaService implements LambdaService {

  private static final Logger log = LoggerFactory.getLogger(LocalFileLambdaService.class);

  private static final Path ROOT_DIR = Paths.get(System.getProperty("user.home"), ".lambda");
  private static final String CONFIG_SUFFIX = "-config.json";
  private static final String CODE_SUFFIX = "-code.jar";
  private static final String CODE_LOC_SUFFIX = "-code-loc.json";

  private final ObjectMapper mapper;

  public LocalFileLambdaService(ObjectMapper objectMapper) {
    this.mapper = objectMapper;
  }

  @Override
  public GetFunctionResult getFunction(String functionName) {
    Path path = Paths.get(ROOT_DIR.toString(), functionName + CONFIG_SUFFIX);
    FunctionConfiguration configuration = (FunctionConfiguration) read(path.toString(), FunctionConfiguration.class);

    GetFunctionResult result = new GetFunctionResult();
    result.setConfiguration(configuration);

    Path codeLocationPath = Paths.get(ROOT_DIR.toString(), functionName + CODE_LOC_SUFFIX);
    FunctionCodeLocation functionCodeLocation = (FunctionCodeLocation) read(codeLocationPath.toString(), FunctionCodeLocation.class);
    result.setCode(functionCodeLocation);

    return result;
  }

  @Override
  public CreateFunctionResult saveFunctionConfiguration(FunctionConfiguration fc) {

    String path = Paths.get(ROOT_DIR.toString(), fc.getFunctionName() + CONFIG_SUFFIX).toString();
    write(path, fc);

    return new CreateFunctionResult()
        .withFunctionName(fc.getFunctionName())
        .withFunctionArn(fc.getFunctionArn())
        .withHandler(fc.getHandler());
  }

  @Override
  public UpdateFunctionConfigurationResult updateFunctionConfiguration(UpdateFunctionConfigurationRequest request) {

    String arn = ArnUtils.of(request.getFunctionName());

    FunctionConfiguration fc = new FunctionConfiguration();
    fc.setFunctionName(request.getFunctionName());
    fc.setFunctionArn(arn);
    fc.setHandler(request.getHandler());
    fc.setRuntime(request.getRuntime());
    fc.setDescription(request.getDescription());
    fc.setMemorySize(request.getMemorySize());

    String path = Paths.get(ROOT_DIR.toString(), fc.getFunctionName() + CONFIG_SUFFIX).toString();
    write(path, fc);

    UpdateFunctionConfigurationResult result = new UpdateFunctionConfigurationResult();
    result.setMemorySize(request.getMemorySize());
    result.setRuntime(request.getRuntime());
    result.setDescription(request.getDescription());
    result.setHandler(request.getHandler());
    result.setFunctionArn(arn);
    result.setFunctionName(request.getFunctionName());

    return result;
  }

  @Override
  public UpdateFunctionCodeResult updateFunctionCode(UpdateFunctionCodeRequest updateFunctionCodeRequest) {
    ByteBuffer byteBuffer = updateFunctionCodeRequest.getZipFile();

    String functionName = updateFunctionCodeRequest.getFunctionName();

    UpdateFunctionCodeResult result = new UpdateFunctionCodeResult();
    result.setFunctionName(functionName);
    result.setFunctionArn(ArnUtils.of(updateFunctionCodeRequest.getFunctionName()));

    FunctionCode code = new FunctionCode();
    code.setZipFile(updateFunctionCodeRequest.getZipFile());

    // write jar with function code to a known file location
    Path codePath = Paths.get(ROOT_DIR.toString(), functionName + CODE_SUFFIX);
    write(codePath.toFile(), byteBuffer.array());

    FunctionCodeLocation functionCodeLocation = new FunctionCodeLocation();
    functionCodeLocation.setLocation(codePath.toString());
    functionCodeLocation.setRepositoryType("Local");

    // write FunctionCodeLocation to file
    Path codeLocationPath = Paths.get(ROOT_DIR.toString(), functionName + CODE_LOC_SUFFIX);
    write(codeLocationPath.toString(), functionCodeLocation);

    return result;
  }

  @Override
  public FunctionConfiguration deleteFunction(String functionName) {
    FunctionConfiguration configuration = null;
    try {
      Path path = Paths.get(ROOT_DIR.toString(), functionName + CONFIG_SUFFIX);
      configuration = (FunctionConfiguration) read(path.toString(), FunctionConfiguration.class);
      delete(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return configuration;
  }

  @Override
  public ListFunctionsResult listFunctions() {
    ListFunctionsResult listFunctionsResult = new ListFunctionsResult();
    PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + ROOT_DIR + "**/*" + CONFIG_SUFFIX);
    try {
      List<FunctionConfiguration> functions = Files.walk(ROOT_DIR)
          .filter(pathMatcher::matches)
          .map(path -> (FunctionConfiguration) read(path.toString(), FunctionConfiguration.class))
          .collect(Collectors.toList());
      listFunctionsResult.setFunctions(functions);

    } catch (IOException e) {
      e.printStackTrace();
    }
    return listFunctionsResult;
  }

  @Override
  public Object invokeFunction(InvokeRequest invokeRequest,
                               FunctionConfiguration functionConfiguration,
                               FunctionCodeLocation functionCodeLocation) {
    // TODO
    return null;
  }

  private void write(File file, byte[] bytes) {
    try (FileOutputStream fileOutputStream = new FileOutputStream(file, false)) {
      fileOutputStream.write(bytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void write(String path, Object object) {
    try {
      mapper.writeValue(new File(path), object);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Object read(String path, Class<?> clazz) {
    try {
      return mapper.readValue(new File(path), clazz);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}

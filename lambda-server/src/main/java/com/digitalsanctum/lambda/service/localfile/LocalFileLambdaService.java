package com.digitalsanctum.lambda.service.localfile;

import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.FunctionCodeLocation;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeResult;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationResult;
import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;
import com.digitalsanctum.lambda.model.DeleteContainerResponse;
import com.digitalsanctum.lambda.model.FunctionContainerConfiguration;
import com.digitalsanctum.lambda.server.util.ArnUtils;
import com.digitalsanctum.lambda.service.LambdaService;
import com.digitalsanctum.lambda.service.LocalFileSystemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.digitalsanctum.lambda.Configuration.CODE_LOC_SUFFIX;
import static com.digitalsanctum.lambda.Configuration.CODE_SUFFIX;
import static com.digitalsanctum.lambda.Configuration.CONFIG_SUFFIX;
import static com.digitalsanctum.lambda.Configuration.CONTAINER_SUFFIX;
import static com.digitalsanctum.lambda.Configuration.ROOT_DIR;
import static java.nio.file.Files.delete;

/**
 * Implementation functionArn LambdaService that persists function code and configuration to the local file system.
 *
 * @author Shane Witbeck
 * @since 10/23/16
 */
public class LocalFileLambdaService implements LambdaService {

  private static final Logger log = LoggerFactory.getLogger(LocalFileLambdaService.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  

  private final LocalFileSystemService localFileSystemService;

  public LocalFileLambdaService(LocalFileSystemService localFileSystemService) {
    this.localFileSystemService = localFileSystemService;
    if (!Files.exists(ROOT_DIR)) {
      log.info("{} not found, creating it", ROOT_DIR.toString());
      try {
        Files.createDirectory(ROOT_DIR);
      } catch (IOException e) {
        log.error("Error creating " + ROOT_DIR.toString(), e);
      }
    }
  }

  @Override
  public GetFunctionResult getFunction(String functionName) {
    Path path = Paths.get(ROOT_DIR.toString(), functionName + CONFIG_SUFFIX);
    FunctionConfiguration configuration = (FunctionConfiguration) localFileSystemService.read(path, FunctionConfiguration.class);
    if (configuration == null) {
      return null;
    }

    GetFunctionResult result = new GetFunctionResult();
    result.setConfiguration(configuration);

    Path codeLocationPath = Paths.get(ROOT_DIR.toString(), functionName + CODE_LOC_SUFFIX);
    FunctionCodeLocation functionCodeLocation = (FunctionCodeLocation) localFileSystemService.read(codeLocationPath, FunctionCodeLocation.class);
    result.setCode(functionCodeLocation);

    return result;
  }

  @Override
  public CreateFunctionResult saveFunctionConfiguration(FunctionConfiguration fc) {

    Path path = Paths.get(ROOT_DIR.toString(), fc.getFunctionName() + CONFIG_SUFFIX);
    localFileSystemService.write(path, fc);

    return new CreateFunctionResult()
        .withFunctionName(fc.getFunctionName())
        .withFunctionArn(fc.getFunctionArn())
        .withHandler(fc.getHandler());
  }

  @Override
  public UpdateFunctionConfigurationResult updateFunctionConfiguration(UpdateFunctionConfigurationRequest request) {

    String arn = ArnUtils.functionArn(request.getFunctionName());

    FunctionConfiguration fc = new FunctionConfiguration();
    fc.setFunctionName(request.getFunctionName());
    fc.setFunctionArn(arn);
    fc.setHandler(request.getHandler());
    fc.setRuntime(request.getRuntime());
    fc.setDescription(request.getDescription());
    fc.setMemorySize(request.getMemorySize());

    Path path = Paths.get(ROOT_DIR.toString(), fc.getFunctionName() + CONFIG_SUFFIX);
    localFileSystemService.write(path, fc);

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
    result.setFunctionArn(ArnUtils.functionArn(updateFunctionCodeRequest.getFunctionName()));

    // TODO make endpoint configurable
    HttpPost post = new HttpPost("http://localhost:" + 8082 + "/images");

    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

      CreateImageRequest request = new CreateImageRequest();
      request.setImageName(updateFunctionCodeRequest.getFunctionName());
      request.setLambdaJar(byteBuffer);

      String requestJson = mapper.writeValueAsString(request);

      StringEntity input = new StringEntity(requestJson);
      post.setEntity(input);

      input.setContentType("application/json");

      CloseableHttpResponse response = httpclient.execute(post);

      HttpEntity entity = response.getEntity();

      String responseJson = EntityUtils.toString(entity, Charsets.UTF_8);
      CreateImageResponse createImageResult = mapper.readValue(responseJson.getBytes(), CreateImageResponse.class);

      FunctionCode code = new FunctionCode();
      code.setZipFile(updateFunctionCodeRequest.getZipFile());

      // write jar with function code to a known file location
      Path codePath = Paths.get(ROOT_DIR.toString(), functionName + CODE_SUFFIX);
      localFileSystemService.write(codePath, byteBuffer.array());

      FunctionCodeLocation functionCodeLocation = new FunctionCodeLocation();
      functionCodeLocation.setLocation(createImageResult.getImageId());
      functionCodeLocation.setRepositoryType("Docker");

      Path codeLocationPath = Paths.get(ROOT_DIR.toString(), functionName + CODE_LOC_SUFFIX);
      localFileSystemService.write(codeLocationPath, functionCodeLocation);

    } catch (IOException e) {
      log.error("Error updating function code", e);
    }
    return result;
  }

  @Override
  public FunctionConfiguration deleteFunction(String functionName) {

    // get containerId from functionName
    Path containerConfigPath = Paths.get(ROOT_DIR.toString(), functionName + CONTAINER_SUFFIX);
    FunctionContainerConfiguration containerConfig = (FunctionContainerConfiguration) localFileSystemService
        .read(containerConfigPath, FunctionContainerConfiguration.class);
    if (containerConfig != null) {

      String containerId = containerConfig.getContainerId();

      // kill and remove container
      try (CloseableHttpClient client = HttpClients.createDefault()) {
        DeleteContainerResponse deleteContainerResponse = deleteContainer(client, containerId);
        log.info(deleteContainerResponse.toString());

      } catch (IOException e) {
        log.error("Error deleting container", e);
      }
    }


    FunctionConfiguration configuration = null;
    try {
      Path path = Paths.get(ROOT_DIR.toString(), functionName + CONFIG_SUFFIX);
      configuration = (FunctionConfiguration) localFileSystemService.read(path, FunctionConfiguration.class);
      delete(path);
    } catch (IOException e) {
      log.error("Error deleting function", e);
    }
    return configuration;
  }

  private DeleteContainerResponse deleteContainer(CloseableHttpClient client, String containerId) throws IOException {
    log.info("Deleting {} container", containerId);
    HttpDelete delete = new HttpDelete("http://localhost:8082/containers/" + containerId);
    CloseableHttpResponse response = client.execute(delete);
    HttpEntity entity = response.getEntity();
    String responseJson = EntityUtils.toString(entity, Charsets.UTF_8);
    return mapper.readValue(responseJson.getBytes(), DeleteContainerResponse.class);
  }

  @Override
  public ListFunctionsResult listFunctions() {
    ListFunctionsResult listFunctionsResult = new ListFunctionsResult();
    PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + ROOT_DIR + "**/*" + CONFIG_SUFFIX);
    try {
      List<FunctionConfiguration> functions = Files.walk(ROOT_DIR)
          .filter(pathMatcher::matches)
          .map(path -> (FunctionConfiguration) localFileSystemService.read(path, FunctionConfiguration.class))
          .collect(Collectors.toList());
      listFunctionsResult.setFunctions(functions);

    } catch (IOException e) {
      log.error("Error listing functions", e);
    }
    return listFunctionsResult;
  }
}

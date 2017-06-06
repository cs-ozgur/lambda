package com.digitalsanctum.lambda.service;

import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCodeLocation;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeResult;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationResult;
import com.digitalsanctum.lambda.model.DeleteContainerResponse;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * @author Shane Witbeck
 * @since 7/17/16
 */
public interface LambdaService {

  GetFunctionResult getFunction(String functionName);

  CreateFunctionResult saveFunctionConfiguration(FunctionConfiguration functionConfiguration);

  UpdateFunctionConfigurationResult updateFunctionConfiguration(UpdateFunctionConfigurationRequest request);

  UpdateFunctionCodeResult updateFunctionCode(UpdateFunctionCodeRequest updateFunctionCodeRequest);

  FunctionConfiguration deleteFunction(String arn);

  DeleteContainerResponse deleteContainer(CloseableHttpClient client, String containerId) throws IOException;

  ListFunctionsResult listFunctions();

  default Object invokeFunction(final InvokeRequest invokeRequest,
                                final String bridgeServerEndpoint,
                                final FunctionConfiguration functionConfiguration,
                                final FunctionCodeLocation functionCodeLocation) {

    String handler = functionConfiguration.getHandler();
    String location = functionCodeLocation.getLocation();
    String name = invokeRequest.getFunctionName();
    Integer timeout = functionConfiguration.getTimeout();

    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

      String functionEndpoint = new ContainerService().runContainer(httpClient, bridgeServerEndpoint,
              handler, location, name, functionConfiguration.getEnvironment().getVariables(), timeout);

      if (functionEndpoint == null) {
        return null;
      }

      ByteBuffer payloadByteBuffer = invokeRequest.getPayload();
      String payload = new String(payloadByteBuffer.array());

      StringEntity input = new StringEntity(payload);
      input.setContentType(APPLICATION_JSON.toString());

      HttpPost post = new HttpPost(functionEndpoint);
      post.setEntity(input);

      CloseableHttpResponse response = httpClient.execute(post);

      HttpEntity entity = response.getEntity();

      return EntityUtils.toString(entity, Charsets.UTF_8);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

}

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
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.ByteBuffer;

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

  ListFunctionsResult listFunctions();

  default Object invokeFunction(InvokeRequest invokeRequest, FunctionConfiguration functionConfiguration,
                        FunctionCodeLocation functionCodeLocation) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    String handler = functionConfiguration.getHandler();
    String location = functionCodeLocation.getLocation();

    String functionEndpoint = new ContainerService().runContainer(httpClient, handler, location);

    try {
      ByteBuffer payloadByteBuffer = invokeRequest.getPayload();
      String payload = new String(payloadByteBuffer.array());

      HttpPost post = new HttpPost(functionEndpoint);
      StringEntity input = new StringEntity(payload);
      post.setEntity(input);
      input.setContentType("application/json");

      CloseableHttpResponse response = httpClient.execute(post);

      HttpEntity entity = response.getEntity();

      return EntityUtils.toString(entity, Charsets.UTF_8);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

}

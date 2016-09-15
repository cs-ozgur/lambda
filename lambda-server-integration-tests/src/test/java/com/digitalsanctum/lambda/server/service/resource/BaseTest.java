package com.digitalsanctum.lambda.server.service.resource;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.digitalsanctum.lambda.functions.model.ConcatRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * @author Shane Witbeck
 * @since 7/25/16
 */
class BaseTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  static final String TEST_FUNCTION_NAME = "concat";

  protected AWSLambda awsLambda;

  String getTestRequest() throws JsonProcessingException {
    ConcatRequest testRequest = new ConcatRequest();
    testRequest.setFirstName("Shane");
    testRequest.setLastName("Witbeck");
    return new String(mapper.writeValueAsBytes(testRequest));
  }

  void invoke_RequestResponse() throws JsonProcessingException {
    String testRequestJson = getTestRequest();

    InvokeRequest invokeRequest = new InvokeRequest();
    invokeRequest.setPayload(testRequestJson);
    invokeRequest.setFunctionName(TEST_FUNCTION_NAME);

    InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
    ByteBuffer byteBuffer = invokeResult.getPayload();
    String resultPayloadJson = new String(byteBuffer.array());
    assertEquals("{\"message\":\"Shane Witbeck\"}", resultPayloadJson);
  }
}
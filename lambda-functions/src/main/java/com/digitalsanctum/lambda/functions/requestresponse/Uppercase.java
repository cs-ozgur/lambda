package com.digitalsanctum.lambda.functions.requestresponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.digitalsanctum.lambda.functions.model.UppercaseRequest;
import com.digitalsanctum.lambda.functions.model.UppercaseResponse;

/**
 * @author Shane Witbeck
 * @since 8/12/16
 */
public class Uppercase implements RequestHandler<UppercaseRequest, UppercaseResponse> {
  public UppercaseResponse handleRequest(UppercaseRequest request, Context context) {
    UppercaseResponse response = new UppercaseResponse();
    response.setOutput(request.getInput().toUpperCase());
    return response;
  }
}

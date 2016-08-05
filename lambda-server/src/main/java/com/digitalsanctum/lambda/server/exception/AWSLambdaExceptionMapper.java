package com.digitalsanctum.lambda.server.exception;

import com.amazonaws.services.lambda.model.AWSLambdaException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static com.amazonaws.http.HttpResponseHandler.X_AMZN_REQUEST_ID_HEADER;

/**
 * @author Shane Witbeck
 * @since 7/24/16
 */
public class AWSLambdaExceptionMapper implements ExceptionMapper<AWSLambdaException> {

  @Override
  public Response toResponse(AWSLambdaException e) {
    return Response
        .status(e.getStatusCode())
        .header("x-amzn-ErrorType", "AWSLambdaException")
        .header(X_AMZN_REQUEST_ID_HEADER, e.getRequestId())
        .entity(e)
        .type(MediaType.APPLICATION_JSON)
        .build();
  }
}

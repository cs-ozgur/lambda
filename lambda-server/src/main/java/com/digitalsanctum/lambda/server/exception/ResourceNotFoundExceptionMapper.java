package com.digitalsanctum.lambda.server.exception;

import com.amazonaws.services.lambda.model.ResourceNotFoundException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static com.amazonaws.http.HttpResponseHandler.X_AMZN_REQUEST_ID_HEADER;

/**
 * @author Shane Witbeck
 * @since 7/23/16
 */
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

  @Override
  public Response toResponse(ResourceNotFoundException e) {
    return Response
        .status(e.getStatusCode())
        .header("x-amzn-ErrorType", "ResourceNotFoundException")
        .header(X_AMZN_REQUEST_ID_HEADER, e.getRequestId())
        .entity(e)
        .type(MediaType.APPLICATION_JSON)
        .build();
  }
}

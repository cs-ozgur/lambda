package com.digitalsanctum.lambda.server.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author Shane Witbeck
 * @since 7/17/16
 */
@Path("/healthcheck")
public class HealthcheckResource {

  @GET
  @Consumes("application/json")
  @Produces("application/json")
  public Response healthcheck() {
    return Response
        .status(Response.Status.OK)
        .entity("ok")
        .build();
  }
}

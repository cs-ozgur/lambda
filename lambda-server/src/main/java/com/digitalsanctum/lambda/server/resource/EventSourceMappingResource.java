package com.digitalsanctum.lambda.server.resource;

import com.digitalsanctum.lambda.server.service.LambdaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Shane Witbeck
 * @since 7/17/16
 */
@Path("/2015-03-31/event-source-mappings")
public class EventSourceMappingResource {
  private static final Logger log = LoggerFactory.getLogger(EventSourceMappingResource.class);

  private final LambdaService lambdaService;

  public EventSourceMappingResource(LambdaService lambdaService) {
    this.lambdaService = lambdaService;
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response create() {
    
    // TODO
    
    return Response
        .status(Response.Status.INTERNAL_SERVER_ERROR)
        .build();
  }

}

package com.digitalsanctum.lambda.server.resource;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsRequest;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import com.digitalsanctum.lambda.server.service.EventSourceMappingService;
import com.digitalsanctum.lambda.server.service.LambdaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static com.amazonaws.http.HttpResponseHandler.X_AMZN_REQUEST_ID_HEADER;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Shane Witbeck
 * @since 7/17/16
 */
@Path("/2015-03-31/event-source-mappings")
public class EventSourceMappingResource {
  private static final Logger log = LoggerFactory.getLogger(EventSourceMappingResource.class);

  private final EventSourceMappingService eventSourceMappingService;
  private final LambdaService lambdaService;

  public EventSourceMappingResource(EventSourceMappingService eventSourceMappingService, LambdaService lambdaService) {
    this.eventSourceMappingService = eventSourceMappingService;
    this.lambdaService = lambdaService;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createFunction(CreateEventSourceMappingRequest request,
                                 @HeaderParam("amz-sdk-invocation-id") String awsSdkInvocationId) {

    log.info("creating event source mapping");

    CreateEventSourceMappingResult result = eventSourceMappingService.createEventSourceMapping(request);

    log.info(result.toString());

    return Response
        .status(Response.Status.CREATED)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
        .entity(result)
        .build();
  }
  
  @GET
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response listFunctions(@QueryParam("eventSourceArn") String eventSourceArn,
                                @QueryParam("functionName") String functionName,
                                @QueryParam("marker") String marker,
                                @QueryParam("maxItems") int maxItems) {

    log.info("listing functions");

    ListEventSourceMappingsRequest request = new ListEventSourceMappingsRequest()
        .withEventSourceArn(eventSourceArn)
        .withFunctionName(functionName)
        .withMarker(marker)
        .withMaxItems(maxItems);    
    
    ListEventSourceMappingsResult result = eventSourceMappingService.listEventSourceMappingConfigurations(request);

    return Response
        .status(Response.Status.OK)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
        .entity(result)
        .build();
  }

  @POST
  @Produces(APPLICATION_JSON)
  public Response create() {
    
    // TODO
    
    return Response
        .status(Response.Status.INTERNAL_SERVER_ERROR)
        .build();
  }

}

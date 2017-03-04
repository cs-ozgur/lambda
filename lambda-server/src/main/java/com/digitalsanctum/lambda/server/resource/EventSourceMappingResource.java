package com.digitalsanctum.lambda.server.resource;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.DeleteEventSourceMappingResult;
import com.amazonaws.services.lambda.model.GetEventSourceMappingResult;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsRequest;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import com.amazonaws.services.lambda.model.UpdateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.UpdateEventSourceMappingResult;
import com.digitalsanctum.lambda.server.service.EventSourceMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
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

  public EventSourceMappingResource(EventSourceMappingService eventSourceMappingService) {
    this.eventSourceMappingService = eventSourceMappingService;
  }

  /**
   * update-event-source-mapping
   *
   * @param updateEventSourceMappingRequest
   * @param uuid
   * @return
   * @see <a href="http://docs.aws.amazon.com/lambda/latest/dg/API_UpdateEventSourceMapping.html">http://docs.aws.amazon.com/lambda/latest/dg/API_UpdateEventSourceMapping.html</a>
   */
  @PUT
  @Path("/{uuid}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response update(UpdateEventSourceMappingRequest updateEventSourceMappingRequest,
                         @PathParam("uuid") String uuid) {

    UpdateEventSourceMappingResult result = eventSourceMappingService.updateEventSourceMappingConfiguration(updateEventSourceMappingRequest);

    return Response
        .status(Response.Status.OK)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
        .entity(result)
        .build();
  }

  /**
   * create-event-source-mapping
   *
   * @param createEventSourceMappingRequest
   * @param awsSdkInvocationId
   * @return
   * @see <a href="http://docs.aws.amazon.com/lambda/latest/dg/API_CreateEventSourceMapping.html">http://docs.aws.amazon.com/lambda/latest/dg/API_CreateEventSourceMapping.html</a>
   */
  @POST
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response create(CreateEventSourceMappingRequest createEventSourceMappingRequest,
                         @HeaderParam("amz-sdk-invocation-id") String awsSdkInvocationId) {
    CreateEventSourceMappingResult result = eventSourceMappingService.createEventSourceMapping(createEventSourceMappingRequest);
    return Response
        .status(Response.Status.CREATED)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
        .entity(result)
        .build();
  }

  /**
   * get-event-source-mapping
   *
   * @param uuid
   * @return
   * @see <a href="http://docs.aws.amazon.com/lambda/latest/dg/API_GetEventSourceMapping.html">http://docs.aws.amazon.com/lambda/latest/dg/API_GetEventSourceMapping.html</a>
   */
  @GET
  @Path("/{uuid}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(@PathParam("uuid") String uuid) {
    GetEventSourceMappingResult result = eventSourceMappingService.getEventSourceMappingConfiguration(uuid);
    return Response
        .status(Response.Status.OK)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
        .entity(result)
        .build();
  }

  /**
   * list-event-source-mappings
   *
   * @param eventSourceArn
   * @param functionName
   * @param marker
   * @param maxItems
   * @return
   * @see <a href="http://docs.aws.amazon.com/lambda/latest/dg/API_ListEventSourceMappings.html">http://docs.aws.amazon.com/lambda/latest/dg/API_ListEventSourceMappings.html</a>
   */
  @GET
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response list(@QueryParam("eventSourceArn") String eventSourceArn,
                       @QueryParam("functionName") String functionName,
                       @QueryParam("marker") String marker,
                       @QueryParam("maxItems") int maxItems) {

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

  /**
   * delete-event-source-mapping
   *
   * @param awsSdkInvocationId
   * @param uuid
   * @return
   * @see <a href="http://docs.aws.amazon.com/lambda/latest/dg/API_DeleteEventSourceMapping.html">http://docs.aws.amazon.com/lambda/latest/dg/API_DeleteEventSourceMapping.html</a>
   */
  @DELETE
  @Path("/{uuid}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@HeaderParam("amz-sdk-invocation-id") String awsSdkInvocationId,
                         @PathParam("uuid") String uuid) {
    DeleteEventSourceMappingResult result = eventSourceMappingService.deleteEventSourceMappingConfiguration(uuid);
    return Response
        .status(Response.Status.ACCEPTED)
        .entity(result)
        .build();
  }

}

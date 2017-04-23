package com.digitalsanctum.lambda.server.resource;

import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.FunctionCodeLocation;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeResult;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationResult;
import com.digitalsanctum.lambda.model.DeleteContainerResponse;
import com.digitalsanctum.lambda.server.util.ArnUtils;
import com.digitalsanctum.lambda.service.LambdaService;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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

import static com.amazonaws.http.HttpResponseHandler.X_AMZN_REQUEST_ID_HEADER;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Endpoints for storing and invoking Lambda functions outside functionArn AWS.
 *
 * @author Shane Witbeck
 * @since 4/24/16
 */
@Path("/2015-03-31/functions")
public class FunctionResource {

  private static final Logger log = LoggerFactory.getLogger(FunctionResource.class);
  
  private static final String X_AMZN_REMAPPED_CONTENT_LENGTH = "x-amzn-Remapped-Content-Length";

  private final LambdaService lambdaService;
  private final String bridgeServerEndpoint;

  public FunctionResource(final LambdaService lambdaService,
                          final String bridgeServerEndpoint) {
    this.lambdaService = lambdaService;
    this.bridgeServerEndpoint = bridgeServerEndpoint;
  }

  @DELETE
  @Path("/{functionName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@HeaderParam("amz-sdk-invocation-id") String awsSdkInvocationId,
                         @PathParam("functionName") String functionName,
                         @QueryParam("Qualifier") String qualifier) throws ResourceNotFoundException {


    GetFunctionResult getFunctionResult = lambdaService.getFunction(functionName);
    verifyFunctionExists(functionName, getFunctionResult);

    lambdaService.deleteFunction(functionName);

    return Response
        .status(NO_CONTENT)
        .header(X_AMZN_REQUEST_ID_HEADER, randomUUID().toString())
        .build();
  }

  /**
   * Implementation functionArn http://docs.aws.amazon.com/lambda/latest/dg/API_CreateFunction.html
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createFunction(CreateFunctionRequest request,
                                 @HeaderParam("amz-sdk-invocation-id") String awsSdkInvocationId) {

    log.info("creating function");

    FunctionConfiguration fc = new FunctionConfiguration();
    fc.setFunctionName(request.getFunctionName());
    fc.setFunctionArn(ArnUtils.functionArn(request.getFunctionName()));
    fc.setRuntime(request.getRuntime());
    fc.setHandler(request.getHandler());

    // save lambda jar to tmp dir and persist the path
    if (request.getCode() != null) {

      log.info("updating function code");

      FunctionCode code = request.getCode();
      UpdateFunctionCodeRequest updateFunctionCodeRequest = new UpdateFunctionCodeRequest();
      updateFunctionCodeRequest.setFunctionName(fc.getFunctionName());
      updateFunctionCodeRequest.setZipFile(code.getZipFile());
      lambdaService.updateFunctionCode(updateFunctionCodeRequest);
    }

    CreateFunctionResult result = lambdaService.saveFunctionConfiguration(fc);

    log.info(result.toString());

    return Response
        .status(CREATED)
        .header(X_AMZN_REQUEST_ID_HEADER, randomUUID().toString())
        .entity(result)
        .build();
  }

  @PUT
  @Path("/{functionName}/code")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateFunctionCode(UpdateFunctionCodeRequest updateFunctionCodeRequest,
                                     @PathParam("functionName") String functionName) {

    GetFunctionResult getFunctionResult = lambdaService.getFunction(functionName);
    verifyFunctionExists(functionName, getFunctionResult);

    updateFunctionCodeRequest.setFunctionName(functionName);

    UpdateFunctionCodeResult updateFunctionCodeResult = lambdaService.updateFunctionCode(updateFunctionCodeRequest);

    // get containerId of existing running container
    String containerId = getFunctionResult.getCode().getLocation();
    
    // delete existing function so that on next execution a new container will be created with new code    
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
    
      DeleteContainerResponse deleteContainerResponse = lambdaService.deleteContainer(httpClient, containerId);
      log.info(deleteContainerResponse.toString());
      
    } catch (Exception e) {
      log.warn("Error deleting container: " + containerId, e);
    }

    return Response
        .status(OK)
        .header(X_AMZN_REQUEST_ID_HEADER, randomUUID().toString())
        .entity(updateFunctionCodeResult)
        .build();
  }

  @PUT
  @Path("/{functionName}/configuration")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateFunctionConfiguration(UpdateFunctionConfigurationRequest updateFunctionConfigurationRequest,
                                              @PathParam("functionName") String functionName) {

    GetFunctionResult getFunctionResult = lambdaService.getFunction(functionName);
    verifyFunctionExists(functionName, getFunctionResult);
    
    UpdateFunctionConfigurationResult updateFunctionConfigurationResult
        = lambdaService.updateFunctionConfiguration(updateFunctionConfigurationRequest);

    return Response
        .status(OK)
        .header(X_AMZN_REQUEST_ID_HEADER, randomUUID().toString())
        .entity(updateFunctionConfigurationResult)
        .build();
  }


  @GET
  @Path("/{functionName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFunction(@PathParam("functionName") String functionName) {

    log.info("getting function {}", functionName);

    GetFunctionResult getFunctionResult = lambdaService.getFunction(functionName);
    verifyFunctionExists(functionName, getFunctionResult);

    return Response
        .status(OK)
        .header(X_AMZN_REQUEST_ID_HEADER, randomUUID().toString())
        .entity(getFunctionResult)
        .build();
  }

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response listFunctions() {

    log.info("listing functions");

    ListFunctionsResult listFunctionsResult = lambdaService.listFunctions();

    return Response
        .status(OK)
        .header(X_AMZN_REQUEST_ID_HEADER, randomUUID().toString())
        .entity(listFunctionsResult)
        .build();
  }

  @POST
  @Path("/{functionName}/invocations")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response invokeFunction(String input,
                                 @PathParam("functionName") String functionName,
                                 @QueryParam("Qualifier") String qualifier) {

    GetFunctionResult getFunctionResult = lambdaService.getFunction(functionName);
    if (getFunctionResult == null) {
      return Response
          .status(NOT_FOUND)
          .header(X_AMZN_REQUEST_ID_HEADER, randomUUID().toString())
          .header(X_AMZN_REMAPPED_CONTENT_LENGTH, 0)
          .build();
    }

    InvokeRequest invokeRequest = new InvokeRequest();
    invokeRequest.setPayload(input);
    invokeRequest.setFunctionName(functionName);
    invokeRequest.setSdkRequestTimeout(30);

    FunctionConfiguration functionConfiguration = getFunctionResult.getConfiguration();
    FunctionCodeLocation functionCodeLocation = getFunctionResult.getCode();


    Object invokeResult = lambdaService.invokeFunction(invokeRequest, bridgeServerEndpoint, functionConfiguration, functionCodeLocation);
    if (invokeResult == null) {
      return Response
          .status(INTERNAL_SERVER_ERROR)
          .header(X_AMZN_REQUEST_ID_HEADER, randomUUID().toString())
          .header(X_AMZN_REMAPPED_CONTENT_LENGTH, 0)
          .build();
    }
    return Response
        .status(OK)
        .header(X_AMZN_REQUEST_ID_HEADER, randomUUID().toString())
        .header(X_AMZN_REMAPPED_CONTENT_LENGTH, 0)
        .entity(invokeResult)
        .build();
  }

  private void verifyFunctionExists(String functionName,
                                    GetFunctionResult getFunctionResult) {
    if (getFunctionResult == null) {
      ResourceNotFoundException e = new ResourceNotFoundException("Function not found: " + ArnUtils.functionArn(functionName));
      e.setErrorCode("ResourceNotFoundException");
      e.setServiceName("AWSLambda");
      e.setStatusCode(NOT_FOUND.getStatusCode());
      e.setRequestId(randomUUID().toString());
      e.setType("User");
      throw e;
    }
  }


}

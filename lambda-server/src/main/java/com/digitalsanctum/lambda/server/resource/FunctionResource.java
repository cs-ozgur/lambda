package com.digitalsanctum.lambda.server.resource;

import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.FunctionCodeLocation;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.PublishVersionRequest;
import com.amazonaws.services.lambda.model.PublishVersionResult;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeResult;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationResult;
import com.digitalsanctum.lambda.server.service.InMemoryLambdaService;
import com.digitalsanctum.lambda.server.service.LambdaService;
import com.digitalsanctum.lambda.server.util.ArnUtils;
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

/**
 * Endpoints for storing and invoking Lambda functions outside of AWS.
 *
 * @author Shane Witbeck
 * @since 4/24/16
 */
@Path("/2015-03-31/functions")
public class FunctionResource {

  private static final Logger log = LoggerFactory.getLogger(FunctionResource.class);

  private final LambdaService lambdaService;

  public FunctionResource(LambdaService lambdaService) {
    this.lambdaService = lambdaService;
  }

  @POST
  @Path("/{functionName}/versions")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response publishVersion(@PathParam("functionName") String functionName,
                                 PublishVersionRequest publishVersionRequest) throws ResourceNotFoundException {


    getFunction(functionName);

    PublishVersionResult result = lambdaService.publish(publishVersionRequest);

    return Response
        .status(Response.Status.OK)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
        .entity(result)
        .build();
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

    FunctionCodeLocation functionCodeLocation = getFunctionResult.getCode();
    System.out.println(functionCodeLocation);

    lambdaService.deleteFunction(functionName);

    return Response
        .status(Response.Status.NO_CONTENT)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
        .build();
  }

  /**
   * Implementation of http://docs.aws.amazon.com/lambda/latest/dg/API_CreateFunction.html
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createFunction(CreateFunctionRequest request,
                                 @HeaderParam("amz-sdk-invocation-id") String awsSdkInvocationId) {

    FunctionConfiguration fc = new FunctionConfiguration();
    fc.setFunctionName(request.getFunctionName());
    fc.setFunctionArn(ArnUtils.of(request.getFunctionName()));
    fc.setRuntime(request.getRuntime());
    fc.setHandler(request.getHandler());

    // save lambda jar to tmp dir and persist the path
    if (request.getCode() != null) {
      FunctionCode code = request.getCode();
      UpdateFunctionCodeRequest updateFunctionCodeRequest = new UpdateFunctionCodeRequest();
      updateFunctionCodeRequest.setFunctionName(fc.getFunctionName());
      updateFunctionCodeRequest.setZipFile(code.getZipFile());
      lambdaService.updateFunctionCode(updateFunctionCodeRequest);
    }

    CreateFunctionResult result = lambdaService.saveFunctionConfiguration(fc);

    return Response
        .status(Response.Status.CREATED)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
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

    UpdateFunctionCodeResult updateFunctionCodeResult = lambdaService.updateFunctionCode(updateFunctionCodeRequest);

    return Response
        .status(Response.Status.OK)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
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
        .status(Response.Status.OK)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
        .entity(updateFunctionConfigurationResult)
        .build();
  }


  @GET
  @Path("/{functionName}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFunction(@PathParam("functionName") String functionName) {

    GetFunctionResult getFunctionResult = lambdaService.getFunction(functionName);
    verifyFunctionExists(functionName, getFunctionResult);

    return Response
        .status(Response.Status.OK)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
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
        .status(Response.Status.OK)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
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
          .status(Response.Status.NOT_FOUND)
          .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
          .header("x-amzn-Remapped-Content-Length", 0)
          .build();
    }

    InvokeRequest invokeRequest = new InvokeRequest();
    invokeRequest.setPayload(input);
    invokeRequest.setFunctionName(functionName);

    FunctionConfiguration functionConfiguration = getFunctionResult.getConfiguration();
    FunctionCodeLocation functionCodeLocation = getFunctionResult.getCode();

    Object invokeResult = lambdaService.invokeFunction(invokeRequest, functionConfiguration, functionCodeLocation);

    return Response
        .status(Response.Status.OK)
        .header(X_AMZN_REQUEST_ID_HEADER, UUID.randomUUID().toString())
        .header("x-amzn-Remapped-Content-Length", 0)
        .entity(invokeResult)
        .build();
  }

  private void verifyFunctionExists(String functionName,
                                    GetFunctionResult getFunctionResult) {
    if (getFunctionResult == null) {
      ResourceNotFoundException e = new ResourceNotFoundException("Function not found: " + ArnUtils.of(functionName));
      e.setErrorCode("ResourceNotFoundException");
      e.setServiceName("AWSLambda");
      e.setStatusCode(Response.Status.NOT_FOUND.getStatusCode());
      e.setRequestId(UUID.randomUUID().toString());
      e.setType("User");
      throw e;
    }
  }


}

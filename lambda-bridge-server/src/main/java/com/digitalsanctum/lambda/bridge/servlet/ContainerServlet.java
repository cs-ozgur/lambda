package com.digitalsanctum.lambda.bridge.servlet;

import com.digitalsanctum.lambda.bridge.service.ContainerService;
import com.digitalsanctum.lambda.bridge.util.RequestUtils;
import com.digitalsanctum.lambda.model.DeleteContainerRequest;
import com.digitalsanctum.lambda.model.DeleteContainerResponse;
import com.digitalsanctum.lambda.model.ListContainersResponse;
import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class ContainerServlet extends HttpServlet {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final ContainerService containerService;

  public ContainerServlet(ContainerService containerService) {
    this.containerService = containerService;
  }

  /**
   * Get all containers
   * 
   * GET /containers
   *
   * @param req
   * @param resp
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    ListContainersResponse listContainersResponse = containerService.listContainers();
    String resultJson = mapper.writeValueAsString(listContainersResponse);

    // write response
    resp.setStatus(listContainersResponse.getStatusCode());
    resp.getWriter().append(resultJson);
  }

  /**
   * Create and run a container
   * 
   * POST /containers
   *
   * @param req
   * @param resp
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String json = RequestUtils.readRequestJson(req);
    RunContainerRequest runContainerRequest = mapper.readValue(json, RunContainerRequest.class);

    // run the container and return the endpoint
    RunContainerResponse runContainerResponse = containerService.createAndRunContainer(runContainerRequest);
    String resultJson = mapper.writeValueAsString(runContainerResponse);

    resp.setStatus(runContainerResponse.getStatusCode());
    resp.getWriter().append(resultJson);
  }

  /**
   * Delete a container
   * 
   * DELETE /containers/699655ad3beedd6d614694d8f2c4754398888f25faf62b6a021c9089c18b275b
   *
   * @param req
   * @param resp
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String pathInfo = req.getPathInfo(); // /699655ad3beedd6d614694d8f2c4754398888f25faf62b6a021c9089c18b275b

    String id = pathInfo.substring(1); // 699655ad3beedd6d614694d8f2c4754398888f25faf62b6a021c9089c18b275b    

    DeleteContainerRequest deleteContainerRequest = new DeleteContainerRequest(id);

    DeleteContainerResponse deleteContainerResponse = containerService.deleteContainer(deleteContainerRequest);
    String resultJson = mapper.writeValueAsString(deleteContainerResponse);

    resp.setStatus(deleteContainerResponse.getStatusCode());
    resp.getWriter().append(resultJson);
  }
}

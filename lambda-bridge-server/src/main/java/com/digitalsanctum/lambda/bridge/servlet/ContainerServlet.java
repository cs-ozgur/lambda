package com.digitalsanctum.lambda.bridge.servlet;

import com.digitalsanctum.lambda.bridge.service.ContainerService;
import com.digitalsanctum.lambda.bridge.util.RequestUtils;
import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.exceptions.DockerException;
import org.apache.http.HttpStatus;

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

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String json = RequestUtils.readRequestJson(req);
    RunContainerRequest runContainerRequest = mapper.readValue(json, RunContainerRequest.class);

    // run the container and return the endpoint
    RunContainerResult runContainerResult = null;
    try {
      runContainerResult = containerService.createAndRunContainer(runContainerRequest);
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
    }
    String resultJson = mapper.writeValueAsString(runContainerResult);

    // write response
    resp.setStatus(HttpStatus.SC_OK);
    resp.getWriter().append(resultJson);
  }
}

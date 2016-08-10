package com.digitalsanctum.lambda.imagebuilder.servlet;

import com.digitalsanctum.lambda.imagebuilder.service.ContainerService;
import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResult;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
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

    // read in the request body (json)
    StringBuilder jb = new StringBuilder();
    String line;
    try {
      BufferedReader reader = req.getReader();
      while ((line = reader.readLine()) != null) {
        jb.append(line);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    RunContainerRequest runContainerRequest = mapper.readValue(jb.toString(), RunContainerRequest.class);

    // run the container and return the endpoint
    RunContainerResult runContainerResult = null;
    try {
      runContainerResult = containerService.createAndRunContainer(runContainerRequest);
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
    }
    String resultJson = mapper.writeValueAsString(runContainerResult);

    // write response
    resp.setStatus(200);
    resp.getWriter().append(resultJson);
  }
}

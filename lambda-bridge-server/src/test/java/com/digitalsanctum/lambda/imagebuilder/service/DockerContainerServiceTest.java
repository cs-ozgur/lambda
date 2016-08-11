package com.digitalsanctum.lambda.imagebuilder.service;

import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class DockerContainerServiceTest {

  private ContainerService dockerContainerService;

  @Before
  public void setup() throws Exception {
    final DockerClient dockerClient = DefaultDockerClient.fromEnv().build();
    dockerContainerService = new DockerContainerService(dockerClient);
  }

  @Test
  public void testCreateAndRunContainer() throws Exception {

    RunContainerRequest request = new RunContainerRequest();
    request.setImageId("lambda");
    request.setHandler("com.digitalsanctum.lambda.samples.HelloPojo");
    
    dockerContainerService.createAndRunContainer(request);
  }
}

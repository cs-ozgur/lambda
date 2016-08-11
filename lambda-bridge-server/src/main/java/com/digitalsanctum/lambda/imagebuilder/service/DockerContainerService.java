package com.digitalsanctum.lambda.imagebuilder.service;

import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResult;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class DockerContainerService implements ContainerService {

  private final DockerClient dockerClient;

  public DockerContainerService(DockerClient dockerClient) {
    this.dockerClient = dockerClient;
  }

  @Override
  public RunContainerResult createAndRunContainer(RunContainerRequest runContainerRequest) throws DockerException, InterruptedException {
    // Bind container ports to host ports
    final String[] ports = {"8080"};
    final Map<String, List<PortBinding>> portBindings = new HashMap<>();
    for (String port : ports) {
      List<PortBinding> hostPorts = new ArrayList<>();
      hostPorts.add(PortBinding.of("0.0.0.0", port));
      portBindings.put(port, hostPorts);
    }

    // Bind container port 8080 to an automatically allocated available host port.
    List<PortBinding> randomPort = new ArrayList<>();
    PortBinding portBinding = PortBinding.randomPort("0.0.0.0");
    randomPort.add(portBinding);
    portBindings.put("8080", randomPort);

    final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

    // Create container with exposed ports
    final ContainerConfig containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .image(runContainerRequest.getImageId())
        .env("LAMBDA_HANDLER=" + runContainerRequest.getHandler())
        .exposedPorts(ports)
        .build();

    final ContainerCreation creation = dockerClient.createContainer(containerConfig);
    final String id = creation.id();
    
    // Start container
    dockerClient.startContainer(id);

    // Inspect container
    final ContainerInfo info = dockerClient.inspectContainer(id);

    String hostPort = (info.networkSettings().ports().get("8080/tcp").get(0)).hostPort();    
    String endpoint = "http://localhost:" + hostPort;

    System.out.println("endpoint=" + endpoint);

    RunContainerResult result = new RunContainerResult();
    result.setName(info.name());
    result.setEndpoint(endpoint);
    result.setHostname(info.config().hostname());
    return result;
  }
}

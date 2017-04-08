package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.DeleteContainerRequest;
import com.digitalsanctum.lambda.model.DeleteContainerResponse;
import com.digitalsanctum.lambda.model.ListContainersResponse;
import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResponse;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class DockerContainerService implements ContainerService {

  private static final Logger log = LoggerFactory.getLogger(DockerContainerService.class);

  private final DockerClient dockerClient;

  public DockerContainerService(DockerClient dockerClient) {
    this.dockerClient = dockerClient;
  }

  @Override
  public RunContainerResponse createAndRunContainer(RunContainerRequest runContainerRequest) {

    try {
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

      log.info("Handler={}, Endpoint={}", runContainerRequest.getHandler(), endpoint);

      return new RunContainerResponse(200, info.name(), info.config().hostname(), endpoint);
    } catch (ContainerNotFoundException cnfe) {
      return new RunContainerResponse(404, cnfe.getMessage());
    } catch (DockerException | InterruptedException de) {
      return new RunContainerResponse(500, de.getMessage());
    }
  }

  @Override
  public DeleteContainerResponse deleteContainer(DeleteContainerRequest deleteContainerRequest) {
    try {
      dockerClient.killContainer(deleteContainerRequest.getContainerId());
      return new DeleteContainerResponse(200);
    } catch (NotFoundException nfe) {
      return new DeleteContainerResponse(404, nfe.getMessage());
    } catch (DockerException | InterruptedException de) {
      return new DeleteContainerResponse(500, de.getMessage());
    }
  }

  @Override public ListContainersResponse listContainers() {

    List<Container> containers;
    try {
      containers = dockerClient.listContainers();
    } catch (DockerException | InterruptedException e) {
      return new ListContainersResponse(500, e.getMessage());
    }

    if (containers == null || containers.isEmpty()) {
      return new ListContainersResponse();
    }

    List<com.digitalsanctum.lambda.model.Container> containers1 = containers.stream()
        .map(container -> new com.digitalsanctum.lambda.model.Container(container.id()))
        .collect(Collectors.toList());    
    return new ListContainersResponse(200, containers1);
  }
}

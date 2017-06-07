package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.DeleteContainerRequest;
import com.digitalsanctum.lambda.model.DeleteContainerResponse;
import com.digitalsanctum.lambda.model.FunctionContainerConfiguration;
import com.digitalsanctum.lambda.model.ListContainersResponse;
import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResponse;
import com.digitalsanctum.lambda.service.LocalFileSystemService;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.digitalsanctum.lambda.Configuration.MAPPING_SUFFIX;
import static com.digitalsanctum.lambda.Configuration.ROOT_DIR;
import static com.digitalsanctum.lambda.model.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static com.digitalsanctum.lambda.model.HttpStatus.SC_NOT_FOUND;
import static com.digitalsanctum.lambda.model.HttpStatus.SC_OK;
import static com.digitalsanctum.lambda.util.LocalUtils.localEndpoint;
import static java.lang.Integer.parseInt;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class DockerContainerService implements ContainerService {

  private static final Logger log = LoggerFactory.getLogger(DockerContainerService.class);

  private static final String CONTAINER_PORT = "8080";
  private static final String DEBUG_PORT = "5005";
  private static final String HOST_BINDING_IP = "0.0.0.0";

  private final DockerClient dockerClient;
  private final LocalFileSystemService localFileSystemService;

  public DockerContainerService(DockerClient dockerClient,
                                LocalFileSystemService localFileSystemService) {
    this.dockerClient = dockerClient;
    this.localFileSystemService = localFileSystemService;
  }

  @Override
  public RunContainerResponse createAndRunContainer(RunContainerRequest runContainerRequest) {

    try {
      // Bind container ports to host ports
      final String[] ports = {CONTAINER_PORT, DEBUG_PORT};
      final Map<String, List<PortBinding>> portBindings = new HashMap<>();
      for (String port : ports) {
        List<PortBinding> hostPorts = new ArrayList<>();
        hostPorts.add(PortBinding.of(HOST_BINDING_IP, port));
        portBindings.put(port, hostPorts);
      }

      // Bind container ports 8080 & 5005 to an automatically allocated available host port.
      for (String port : ports) {
        portBindings.put(port, Collections.singletonList(PortBinding.randomPort(HOST_BINDING_IP)));
      }

      final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

      List<String> keyValuePairs = createEnvironmentVariableList(runContainerRequest);

      // Create container with exposed ports
      final ContainerConfig containerConfig = ContainerConfig.builder()
              .hostConfig(hostConfig)
              .image(runContainerRequest.getImageId())
              .env(keyValuePairs.toArray(new String[keyValuePairs.size()]))
              .exposedPorts(ports)
              .build();

      final String functionName = runContainerRequest.getName();
      final ContainerCreation creation = dockerClient.createContainer(containerConfig, functionName);
      final String id = creation.id();

      // Start container
      dockerClient.startContainer(id);

      // Inspect container
      final ContainerInfo info = dockerClient.inspectContainer(id);

      String hostPort = (info.networkSettings().ports().get(CONTAINER_PORT + "/tcp").get(0)).hostPort();
      String endpoint = localEndpoint(parseInt(hostPort));

      // write functionName/containerId mapping file
      Path path = Paths.get(ROOT_DIR.toString(), functionName + MAPPING_SUFFIX);
      FunctionContainerConfiguration functionContainerConfiguration = new FunctionContainerConfiguration(functionName, id);
      localFileSystemService.write(path, functionContainerConfiguration);

      log.info("Handler={}, Endpoint={}", runContainerRequest.getHandler(), endpoint);

      return new RunContainerResponse(id, info.name(), info.config().hostname(), endpoint);

    } catch (ContainerNotFoundException cnfe) {
      return new RunContainerResponse(SC_NOT_FOUND, cnfe.getMessage());
    } catch (DockerException | InterruptedException de) {
      if (de instanceof DockerRequestException) {
        return new RunContainerResponse(((DockerRequestException) de).status(), de.getMessage());
      }
      return new RunContainerResponse(SC_INTERNAL_SERVER_ERROR, de.getMessage());
    }
  }

  private List<String> createEnvironmentVariableList(RunContainerRequest runContainerRequest) {
    /**
     * Create list initially with request parameter.
     */
    List<String> keyValuePairs = runContainerRequest.getEnvironmentVariables().entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.toList());

    keyValuePairs.add("LAMBDA_HANDLER=" + runContainerRequest.getHandler());
    keyValuePairs.add("LAMBDA_TIMEOUT=" + runContainerRequest.getTimeout());

    return keyValuePairs;
  }

  @Override
  public DeleteContainerResponse deleteContainer(DeleteContainerRequest deleteContainerRequest) {
    try {
      String containerId = deleteContainerRequest.getContainerId();
      dockerClient.killContainer(containerId);
      dockerClient.removeContainer(containerId);
      return new DeleteContainerResponse(SC_OK);
    } catch (NotFoundException nfe) {
      return new DeleteContainerResponse(SC_NOT_FOUND, nfe.getMessage());
    } catch (DockerException | InterruptedException de) {
      return new DeleteContainerResponse(SC_INTERNAL_SERVER_ERROR, de.getMessage());
    }
  }

  @Override public ListContainersResponse listContainers() {

    List<Container> containers;
    try {
      containers = dockerClient.listContainers();
    } catch (DockerException | InterruptedException e) {
      return new ListContainersResponse(SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }

    if (containers == null || containers.isEmpty()) {
      return new ListContainersResponse();
    }

    return new ListContainersResponse(SC_OK, containers.stream()
            .map(container -> new com.digitalsanctum.lambda.model.Container(container.id(), container.names().get(0)))
            .collect(Collectors.toList()));
  }
}

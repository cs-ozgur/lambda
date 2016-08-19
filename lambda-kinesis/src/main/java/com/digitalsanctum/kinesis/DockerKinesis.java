package com.digitalsanctum.kinesis;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 8/19/16
 */
public class DockerKinesis implements Kinesis {
  private static final Logger log = LoggerFactory.getLogger(DockerKinesis.class);

  private static final String DEFAULT_KINESIS_CONTAINER_PORT = "4567";
  private static final String DEFAULT_KINESIS_DOCKER_IMAGE = "instructure/kinesalite";

  private boolean running = false;
  private String containerId;
  private String imageId = DEFAULT_KINESIS_DOCKER_IMAGE;
  private String containerPort = DEFAULT_KINESIS_CONTAINER_PORT;

  private final DockerClient dockerClient;

  public DockerKinesis() {
    this.dockerClient = initDefaultClient();
  }

  public DockerKinesis(DockerClient dockerClient) {
    this.dockerClient = dockerClient;
  }

  public DockerKinesis(DockerClient dockerClient, String imageId, String containerPort) {
    this.dockerClient = dockerClient;
    this.imageId = imageId;
    this.containerPort = containerPort;
  }

  private DockerClient initDefaultClient() {
    DockerClient dockerClient = null;
    try {
      dockerClient = DefaultDockerClient.fromEnv().build();
      Version version = dockerClient.version();
      log.info(version.toString());
    } catch (DockerCertificateException | DockerException | InterruptedException e) {
      log.error("Error connecting to Docker", e);
    }
    return dockerClient;
  }

  @Override
  public int start() {
    int port = -1;
    try {
      ContainerInfo info = createAndRunContainer(imageId);
      String hostPort = (info.networkSettings().ports().get(containerPort + "/tcp").get(0)).hostPort();
      port = Integer.parseInt(hostPort);
      this.containerId = info.id();
      running = true;
      log.info("Kinesis running on port " + hostPort);
    } catch (DockerException | InterruptedException e) {
      log.error("Error running container from image {}", imageId, e);
    }
    return port;
  }

  @Override
  public void stop() {
    if (running && containerId != null) {
      try {
        log.info("killing container {}...", containerId);
        dockerClient.killContainer(containerId);
        running = false;
        containerId = null;
      } catch (DockerException | InterruptedException e) {
        log.error("Error killing container {}", containerId, e);
      }
    } else {
      if (containerId == null) {
        log.warn("No container found running");
      } else {
        log.warn("No container found running with id {}", containerId);
      }
    }
  }

  @Override
  public boolean isRunning() {
    return this.running;
  }

  public ContainerInfo createAndRunContainer(String imageId) throws DockerException, InterruptedException {

    // verify image is available/present
    try {
      dockerClient.inspectImage(imageId);
    } catch (ImageNotFoundException e) {
      log.warn("image {} not found; pulling...", imageId);
      dockerClient.pull(imageId);
    }

    // Bind container ports to host ports
    final String[] ports = {containerPort};
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
    portBindings.put(containerPort, randomPort);

    final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

    // Create container with exposed ports
    final ContainerConfig containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .image(imageId)
        .exposedPorts(ports)
        .build();

    final ContainerCreation creation = dockerClient.createContainer(containerConfig);
    final String id = creation.id();

    // Start container
    dockerClient.startContainer(id);

    // Inspect container
    return dockerClient.inspectContainer(id);
  }
}

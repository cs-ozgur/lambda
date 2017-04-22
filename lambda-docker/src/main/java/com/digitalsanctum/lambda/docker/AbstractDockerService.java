package com.digitalsanctum.lambda.docker;

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

import static java.lang.Integer.parseInt;

/**
 * @author Shane Witbeck
 * @since 8/25/16
 */
public abstract class AbstractDockerService implements DockerService {

  private static final Logger log = LoggerFactory.getLogger(AbstractDockerService.class);

  private String containerId;
  private boolean running = false;

  private final DockerClient dockerClient;

  public AbstractDockerService() {
    this.dockerClient = initDefaultClient();
  }

  public AbstractDockerService(DockerClient dockerClient) {
    this.dockerClient = dockerClient;
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
      ContainerInfo info = createAndRunContainer(imageId());
      String hostPort = (info.networkSettings().ports().get(containerPort() + "/tcp").get(0)).hostPort();
      port = parseInt(hostPort);
      this.containerId = info.id();
      running = true;
      log.info("{} running on port {}", name(), hostPort);
    } catch (DockerException | InterruptedException e) {
      log.error("Error running {} container from image {}", name(), imageId(), e);
    }
    return port;
  }

  private ContainerInfo createAndRunContainer(String imageId, String... volumes) throws DockerException, InterruptedException {

    // verify image is available/present
    try {
      dockerClient.inspectImage(imageId);
    } catch (ImageNotFoundException e) {
      log.warn("image {} not found; pulling...", imageId);
      dockerClient.pull(imageId);
    }

    // Bind container ports to host ports
    final String[] ports = {String.valueOf(containerPort())};
    final Map<String, List<PortBinding>> portBindings = new HashMap<>();
    for (String port : ports) {
      List<PortBinding> hostPorts = new ArrayList<>();
      hostPorts.add(PortBinding.of("0.0.0.0", port));
      portBindings.put(port, hostPorts);
    }

    // Bind containerPort() to an automatically allocated available host port.
    List<PortBinding> randomPort = new ArrayList<>();
    PortBinding portBinding = PortBinding.randomPort("0.0.0.0");
    randomPort.add(portBinding);
    portBindings.put(String.valueOf(containerPort()), randomPort);

    final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

    // Create container with exposed ports
    final ContainerConfig.Builder containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .image(imageId)
        .addVolumes(volumes)
        .exposedPorts(ports);
    
    final ContainerCreation creation = dockerClient.createContainer(containerConfig.build(), name());
    final String id = creation.id();

    // Start container
    dockerClient.startContainer(id);

    // Inspect container
    return dockerClient.inspectContainer(id);
  }

  @Override
  public void stop() {
    if (running && containerId != null) {
      try {
        log.info("killing container {} with id {}...", name(), containerId);
        dockerClient.killContainer(containerId);
        running = false;
        containerId = null;
      } catch (DockerException | InterruptedException e) {
        log.error("Error killing {} container with id {}", name(), containerId, e);
      }
    } else {
      if (containerId == null) {
        log.warn("No {} container found running", name());
      } else {
        log.warn("No {} container found running with id {}", name(), containerId);
      }
    }
  }

  @Override
  public boolean isRunning() {
    return this.running;
  }
}

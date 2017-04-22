package com.digitalsanctum.lambda.bridge.server;

import com.digitalsanctum.lambda.bridge.service.ContainerService;
import com.digitalsanctum.lambda.bridge.service.DockerContainerService;
import com.digitalsanctum.lambda.bridge.service.DockerImageService;
import com.digitalsanctum.lambda.bridge.service.ImageService;
import com.digitalsanctum.lambda.bridge.servlet.ContainerServlet;
import com.digitalsanctum.lambda.bridge.servlet.ImageServlet;
import com.digitalsanctum.lambda.service.LocalFileSystemService;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Version;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.digitalsanctum.lambda.model.Component.Bridge;

/**
 * @author Shane Witbeck
 * @since 8/8/16
 */
public class DockerBridgeServer {

  private static final Logger log = LoggerFactory.getLogger(DockerBridgeServer.class);

  private Server server;
  private int port;
  private boolean running = false;

  public DockerBridgeServer(int port) throws DockerCertificateException {
    this.port = port;

    ServletContextHandler sch = new ServletContextHandler();
    sch.setContextPath("/");
    
    final DockerClient dockerClient = DefaultDockerClient.fromEnv().build();
    try {
      Version version = dockerClient.version();
      log.info(version.toString());
    } catch (DockerException | InterruptedException e) {
      log.error("Error connecting to Docker", e);
      System.exit(1);
    }

    ImageService imageService = new DockerImageService(dockerClient);
    ImageServlet builderServlet = new ImageServlet(imageService);
    
    ServletHolder imageServletHolder = new ServletHolder(builderServlet);
    sch.addServlet(imageServletHolder, "/images/*");

    LocalFileSystemService localFileSystemService = new LocalFileSystemService();
    ContainerService containerService = new DockerContainerService(dockerClient, localFileSystemService);
    ContainerServlet containerServlet = new ContainerServlet(containerService);
    ServletHolder containerServiceHolder = new ServletHolder(containerServlet);
    sch.addServlet(containerServiceHolder, "/containers/*");
    
    server = new Server(port);
    server.setHandler(sch);

    // remove server header
    for (Connector y : server.getConnectors()) {
      y.getConnectionFactories().stream()
          .filter(x -> x instanceof HttpConnectionFactory)
          .forEach(x -> ((HttpConnectionFactory) x).getHttpConfiguration().setSendServerVersion(false));
    }
  }

  public void start() {
    try {
      server.start();
      running = true;
      log.info("{} started on port {}", Bridge, port);
    } catch (Exception e) {
      log.error("Error starting " + Bridge, e);
    }  
  }

  public void stop() {
    try {
      server.stop();
      running = false;
      log.info("{} stopped", Bridge);
    } catch (Exception e) {
      log.error("Error stopping " + Bridge, e);
    }
  }

  public int getPort() {
    return port;
  }

  public boolean isRunning() {
    return running;
  }

  public static void main(String[] args) throws Exception {

    int port = 8082;
    if (args != null && args.length == 1) {
      port = Integer.parseInt(args[0]);
    }

    DockerBridgeServer dockerBridgeServer = new DockerBridgeServer(port);
    dockerBridgeServer.start();
  }
}

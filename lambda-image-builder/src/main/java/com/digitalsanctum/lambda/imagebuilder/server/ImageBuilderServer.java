package com.digitalsanctum.lambda.imagebuilder.server;

import com.digitalsanctum.lambda.imagebuilder.service.ContainerService;
import com.digitalsanctum.lambda.imagebuilder.service.DockerContainerService;
import com.digitalsanctum.lambda.imagebuilder.service.DockerImageBuilder;
import com.digitalsanctum.lambda.imagebuilder.service.ImageBuilder;
import com.digitalsanctum.lambda.imagebuilder.servlet.BuilderServlet;
import com.digitalsanctum.lambda.imagebuilder.servlet.ContainerServlet;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shane Witbeck
 * @since 8/8/16
 */
public class ImageBuilderServer {

  private static final Logger log = LoggerFactory.getLogger(ImageBuilderServer.class);

  private Server server;
  private int port;

  public ImageBuilderServer(int port) throws DockerCertificateException {
    this.port = port;

    ServletContextHandler sch = new ServletContextHandler();
    sch.setContextPath("/");
    
    final DockerClient dockerClient = DefaultDockerClient.fromEnv().build();

    ImageBuilder imageBuilder = new DockerImageBuilder(dockerClient);
    BuilderServlet builderServlet = new BuilderServlet(imageBuilder);
    
    ServletHolder builderServletHolder = new ServletHolder(builderServlet);
    sch.addServlet(builderServletHolder, "/images/*");

    ContainerService containerService = new DockerContainerService(dockerClient);
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

  public void start() throws Exception {
    server.start();
    log.info("started on port {}", port);
  }

  public void stop() throws Exception {
    server.stop();
    log.info("stopped");
  }

  public static void main(String[] args) throws Exception {

    int port = 8082;
    if (args != null && args.length == 1) {
      port = Integer.parseInt(args[0]);
    }

    ImageBuilderServer imageBuilderServer = new ImageBuilderServer(port);
    imageBuilderServer.start();
  }
}

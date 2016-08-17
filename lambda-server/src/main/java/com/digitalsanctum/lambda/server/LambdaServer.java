package com.digitalsanctum.lambda.server;

import com.digitalsanctum.lambda.server.exception.AWSLambdaExceptionMapper;
import com.digitalsanctum.lambda.server.exception.ResourceNotFoundExceptionMapper;
import com.digitalsanctum.lambda.server.filter.AWSFilter;
import com.digitalsanctum.lambda.server.resource.EventSourceMappingResource;
import com.digitalsanctum.lambda.server.resource.FunctionResource;
import com.digitalsanctum.lambda.server.resource.HealthcheckResource;
import com.digitalsanctum.lambda.server.service.InMemoryLambdaService;
import com.digitalsanctum.lambda.server.service.LambdaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * @author Shane Witbeck
 * @since 4/24/16
 */
public class LambdaServer {

  private static final Logger log = LoggerFactory.getLogger(LambdaServer.class);

  private Server server;
  private int port;
  private boolean running = false;

  public LambdaServer(int port) {
    this.port = port;
    ResourceConfig rc = new ResourceConfig();

    // create custom ObjectMapper for AWS SDK
    final ObjectMapper awsSdkObjectMapper = new ObjectMapper();
    awsSdkObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UpperCamelCaseStrategy.UPPER_CAMEL_CASE);

    // create JsonProvider to provide custom ObjectMapper
    JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
    provider.setMapper(awsSdkObjectMapper);
    rc.register(provider);

    // resources
    final ObjectMapper mapper = new ObjectMapper();
    LambdaService lambdaService = new InMemoryLambdaService(mapper);
    EventSourceMappingResource eventSourceMappingResource = new EventSourceMappingResource(lambdaService);
    FunctionResource functionResource = new FunctionResource(lambdaService);
    rc.register(eventSourceMappingResource);
    rc.register(functionResource);
    rc.register(new HealthcheckResource());

    // AWS exception mappers
    rc.register(new AWSLambdaExceptionMapper());
    rc.register(new ResourceNotFoundExceptionMapper());

    ServletContainer sc = new ServletContainer(rc);
    ServletHolder holder = new ServletHolder(sc);
    ServletContextHandler sch = new ServletContextHandler();
    sch.setContextPath("/");
    sch.addServlet(holder, "/*");
    
    // Content-Type header fix
    sch.addFilter(AWSFilter.class, "/*", EnumSet.copyOf(Arrays.asList(DispatcherType.values())));

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
    running = true;
    log.info("started on port {}", port);
  }

  public void stop() throws Exception {
    server.stop();
    running = false;
    log.info("stopped");
  }

  public boolean isRunning() {
    return running;
  }

  public static void main(String[] args) throws Exception {
    
    int port = 8080;
    if (args != null && args.length == 1) {
      port = Integer.parseInt(args[0]);
    }
    
    LambdaServer lambdaServer = new LambdaServer(port);
    lambdaServer.start();
  }
}

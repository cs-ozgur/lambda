package com.digitalsanctum.lambda.server;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import com.digitalsanctum.lambda.server.exception.AWSLambdaExceptionMapper;
import com.digitalsanctum.lambda.server.exception.ResourceNotFoundExceptionMapper;
import com.digitalsanctum.lambda.server.filter.AWSFilter;
import com.digitalsanctum.lambda.server.resource.EventSourceMappingResource;
import com.digitalsanctum.lambda.server.resource.FunctionResource;
import com.digitalsanctum.lambda.server.resource.HealthcheckResource;
import com.digitalsanctum.lambda.service.EventSourceMappingService;
import com.digitalsanctum.lambda.service.LambdaService;
import com.digitalsanctum.lambda.service.LocalFileSystemService;
import com.digitalsanctum.lambda.service.localfile.LocalFileEventSourceMappingService;
import com.digitalsanctum.lambda.service.localfile.LocalFileLambdaService;
import com.digitalsanctum.lambda.service.serialization.CreateEventSourceMappingResultSerializer;
import com.digitalsanctum.lambda.service.serialization.EventSourceMappingConfigurationSerializer;
import com.digitalsanctum.lambda.service.serialization.ListEventSourceMappingsResultSerializer;
import com.digitalsanctum.lambda.util.LocalUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
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

import static java.util.EnumSet.copyOf;

/**
 * @author Shane Witbeck
 * @since 4/24/16
 */
public class LambdaServer {

  private static final Logger log = LoggerFactory.getLogger(LambdaServer.class);

  private Server server;
  private int port;
  private boolean running = false;
  private final EventSourceMappingService eventSourceMappingService;
  private final LambdaService lambdaService;

  public LambdaServer(int port, 
                      String bridgeServerEndpoint,
                      LambdaService lambdaService,
                      EventSourceMappingService eventSourceMappingService) {
    this.port = port;
    this.lambdaService = lambdaService;
    this.eventSourceMappingService = eventSourceMappingService;
    
    ResourceConfig rc = new ResourceConfig();

    // create custom ObjectMapper for AWS SDK
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UpperCamelCaseStrategy.UPPER_CAMEL_CASE);

    // custom serializers
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(CreateEventSourceMappingResult.class, new CreateEventSourceMappingResultSerializer());
    simpleModule.addSerializer(EventSourceMappingConfiguration.class, new EventSourceMappingConfigurationSerializer());
    simpleModule.addSerializer(ListEventSourceMappingsResult.class, new ListEventSourceMappingsResultSerializer());
    objectMapper.registerModule(simpleModule);
    
    // create JsonProvider to provide custom ObjectMapper
    JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
    provider.setMapper(objectMapper);
    rc.register(provider);

    // resources    
    EventSourceMappingResource eventSourceMappingResource = new EventSourceMappingResource(eventSourceMappingService);
    
    FunctionResource functionResource = new FunctionResource(lambdaService, bridgeServerEndpoint);
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
    sch.addFilter(AWSFilter.class, "/*", copyOf(Arrays.asList(DispatcherType.values())));

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
      log.info("Lambda started on port {}", port);
    } catch (Exception e) {
      log.error("Error starting Lambda", e);
    }
  }

  public void stop() {
    try {
      server.stop();
      running = false;
      log.info("Lambda stopped");
    } catch (Exception e) {
      log.error("Error stopping Lambda", e);
    }
  }

  public boolean isRunning() {
    return running;
  }

  public static void main(String[] args) throws Exception {
    
    int port = 8080;
    if (args != null && args.length == 1) {
      port = Integer.parseInt(args[0]);
    }
    
    String bridgeServerEndpoint = LocalUtils.localEndpoint(8082);
    
    LocalFileSystemService localFileSystemService = new LocalFileSystemService();
    LambdaServer lambdaServer = new LambdaServer(
        port,
        bridgeServerEndpoint,
        new LocalFileLambdaService(localFileSystemService), 
        new LocalFileEventSourceMappingService(localFileSystemService)
    );
    lambdaServer.start();
  }
}

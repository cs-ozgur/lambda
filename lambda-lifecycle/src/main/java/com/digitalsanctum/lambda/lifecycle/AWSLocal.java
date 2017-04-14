package com.digitalsanctum.lambda.lifecycle;

import com.amazonaws.SDKGlobalConfiguration;
import com.digitalsanctum.dynamodb.DockerDynamoDB;
import com.digitalsanctum.kinesis.DockerKinesis;
import com.digitalsanctum.lambda.bridge.server.DockerBridgeServer;
import com.digitalsanctum.lambda.model.Component;
import com.digitalsanctum.lambda.server.LambdaServer;
import com.digitalsanctum.lambda.service.inmemory.InMemoryEventSourceMappingService;
import com.digitalsanctum.lambda.service.inmemory.InMemoryLambdaService;
import com.digitalsanctum.lambda.service.localfile.LocalFileEventSourceMappingService;
import com.digitalsanctum.lambda.service.localfile.LocalFileLambdaService;
import com.digitalsanctum.lambda.service.localfile.LocalFileSystemService;
import com.digitalsanctum.s3.DockerS3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

import static com.digitalsanctum.lambda.lifecycle.AWSLocal.LambdaServiceType.FILESYSTEM;
import static com.digitalsanctum.lambda.lifecycle.AWSLocal.LambdaServiceType.IN_MEMORY;
import static com.digitalsanctum.lambda.model.Component.Bridge;
import static com.digitalsanctum.lambda.model.Component.DynamoDB;
import static com.digitalsanctum.lambda.model.Component.KinesisStreams;
import static com.digitalsanctum.lambda.model.Component.Lambda;
import static com.digitalsanctum.lambda.model.Component.S3;

/**
 * @author Shane Witbeck
 * @since 3/9/17
 */
public class AWSLocal implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(AWSLocal.class);

  public static final String SIGNING_REGION = "local";
  private static final int BRIDGE_SERVER_PORT = 8082;
  private static final int LAMBDA_SERVER_PORT = 8080;
  private static final String HOST = "localhost";
  
  private boolean kinesisStreamsEnabled;
  private int kinesisPort;
  private String kinesisEndpoint;
  
  private boolean dynamoDbEnabled;
  private int dynamoDbPort;
  private String dynamoDbEndpoint;
  
  private boolean s3Enabled;
  private int s3Port;
  private String s3Endpoint;

  private LambdaServiceType lambdaServiceType;

  private static LambdaServer lambdaServer;
  private static DockerBridgeServer dockerBridgeServer;
  private static DockerDynamoDB dockerDynamoDB;
  private static DockerKinesis dockerKinesis;
  private static DockerS3 dockerS3;
  
  private static Map<Component, String> endpoints = new TreeMap<>();
  
  private AWSLocal(Builder builder) {
    this.dynamoDbEnabled = builder.enableDynamoDB;
    this.kinesisStreamsEnabled = builder.enableKinesisStreams;
    this.lambdaServiceType = builder.lambdaServiceType;
    this.s3Enabled = builder.enableS3;
  }

  public static void main(String[] args) {
    AWSLocal.builder(FILESYSTEM)
        .enableDynamoDB()
        .enableKinesisStreams()
        .enableS3()
        .build()
        .start()
        .dumpEndpoints();
  }
  
  public void dumpEndpoints() {
    endpoints.forEach((key, value) -> log.info("{} -> {}", key, value));
  }
  
  private String localEndpoint(int port) {
    String endpoint = null;
    try {
      endpoint = new URL("http", HOST, port, "").toString();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return endpoint;
  }
  
  public AWSLocal start() {
    if (isDynamoDbEnabled()) {
      dockerDynamoDB = new DockerDynamoDB();
      this.dynamoDbPort = dockerDynamoDB.start();
      this.dynamoDbEndpoint = localEndpoint(this.dynamoDbPort);      
      endpoints.put(DynamoDB, this.dynamoDbEndpoint);
    }
    
    if (isKinesisStreamsEnabled()) {
      dockerKinesis = new DockerKinesis();
      this.kinesisPort = dockerKinesis.start();
      this.kinesisEndpoint = localEndpoint(kinesisPort);
      endpoints.put(KinesisStreams, this.kinesisEndpoint);
    }
    
    if (isS3Enabled()) {
      dockerS3 = new DockerS3();
      this.s3Port = dockerS3.start();
      this.s3Endpoint = localEndpoint(s3Port);
      endpoints.put(S3, this.s3Endpoint);
    }

    // make sure to kill containers
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      log.info("shutting down containers from shutdown hook");
      if (isKinesisStreamsEnabled()) {
        dockerKinesis.stop();
      }
      if (isDynamoDbEnabled()) {
        dockerDynamoDB.stop();
      }
      if (isS3Enabled()) {
        dockerS3.stop();
      }
      dockerBridgeServer.stop();
      lambdaServer.stop();
    }));

    if (lambdaServiceType != null) {
      try {
        dockerBridgeServer = new DockerBridgeServer(BRIDGE_SERVER_PORT);
        dockerBridgeServer.start();
      } catch (Exception e) {
        log.error("Error starting BridgeServer", e);
      }
      endpoints.put(Bridge, localEndpoint(BRIDGE_SERVER_PORT));

      if (IN_MEMORY.equals(this.lambdaServiceType)) {        
        lambdaServer = new LambdaServer(LAMBDA_SERVER_PORT, new InMemoryLambdaService(), new InMemoryEventSourceMappingService());        
      } else if (FILESYSTEM.equals(this.lambdaServiceType)) {
        LocalFileSystemService localFileSystemService = new LocalFileSystemService();
        lambdaServer = new LambdaServer(LAMBDA_SERVER_PORT, new LocalFileLambdaService(localFileSystemService), new LocalFileEventSourceMappingService(localFileSystemService));
      }           

      try {
        lambdaServer.start();
        endpoints.put(Lambda, localEndpoint(LAMBDA_SERVER_PORT));
      } catch (Exception e) {
        log.error("Error starting LambdaServer", e);
      }
    }
    
    return this;
  }

  public LambdaServiceType getLambdaServiceType() {
    return lambdaServiceType;
  }

  public boolean isKinesisStreamsEnabled() {
    return kinesisStreamsEnabled;
  }

  public boolean isS3Enabled() {
    return s3Enabled;
  }

  public boolean isDynamoDbEnabled() {
    return dynamoDbEnabled;
  }
  
  public boolean isLambdaEnabled() {
    return this.lambdaServiceType != null;
  }

  public String getSigningRegion() {
    return SIGNING_REGION;
  }

  public int getKinesisPort() {
    return kinesisPort;
  }

  public String getKinesisEndpoint() {
    return kinesisEndpoint;
  }

  public int getDynamoDbPort() {
    return dynamoDbPort;
  }

  public String getDynamoDbEndpoint() {
    return dynamoDbEndpoint;
  }

  public static LambdaServer getLambdaServer() {
    return lambdaServer;
  }

  public static Builder builder(LambdaServiceType lambdaServiceType) {
    return new Builder(lambdaServiceType);
  }
  
  public void stop() {
    if (lambdaServer.isRunning()) {
      log.info("stopping LambdaServer");
      try {
        lambdaServer.stop();
      } catch (Exception e) {
        log.error("Error stopping LambdaServer", e);
      }
    }
    if (dockerBridgeServer.isRunning()) {
      log.info("stopping DockerBridgeServer");
      try {
        dockerBridgeServer.stop();
      } catch (Exception e) {
        log.error("Error stopping BridgeServer", e);
      }
    }

    if (isKinesisStreamsEnabled() && dockerKinesis != null) {
      log.info("stopping Kinesis container");
      dockerKinesis.stop();
      System.setProperty(SDKGlobalConfiguration.AWS_CBOR_DISABLE_SYSTEM_PROPERTY, "");
    }
    
    if (isDynamoDbEnabled() && dockerDynamoDB != null) {
      log.info("stopping DynamoDB container");
      dockerDynamoDB.stop();
    }
    
    if (isS3Enabled() && dockerS3 != null) {
      log.info("stopping S3 container");
      dockerS3.stop();
    }
  }

  @Override
  public void close() throws IOException {
    stop();
  }

  public enum LambdaServiceType {
    IN_MEMORY,
    FILESYSTEM
  }

  public static class Builder {
    private boolean enableKinesisStreams = false;
    private boolean enableDynamoDB = false;
    private boolean enableS3 = false;
    private LambdaServiceType lambdaServiceType;

    public Builder(LambdaServiceType lambdaServiceType) {
      this.lambdaServiceType = lambdaServiceType;
    }

    public Builder enableKinesisStreams() {
      this.enableKinesisStreams = true;
      return this;
    }

    public Builder enableDynamoDB() {
      this.enableDynamoDB = true;
      return this;
    }
    
    public Builder enableS3() {
      this.enableS3 = true;
      return this;
    }
    
    public AWSLocal build() {
      return new AWSLocal(this);
    }
  }
}

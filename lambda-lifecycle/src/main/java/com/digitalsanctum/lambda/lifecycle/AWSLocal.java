package com.digitalsanctum.lambda.lifecycle;

import com.amazonaws.SDKGlobalConfiguration;
import com.digitalsanctum.dynamodb.DockerDynamoDB;
import com.digitalsanctum.kinesis.DockerKinesis;
import com.digitalsanctum.lambda.bridge.server.DockerBridgeServer;
import com.digitalsanctum.lambda.server.LambdaServer;
import com.digitalsanctum.lambda.service.inmemory.InMemoryEventSourceMappingService;
import com.digitalsanctum.lambda.service.inmemory.InMemoryLambdaService;
import com.digitalsanctum.lambda.service.localfile.LocalFileEventSourceMappingService;
import com.digitalsanctum.lambda.service.localfile.LocalFileLambdaService;
import com.digitalsanctum.lambda.service.localfile.LocalFileSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @author Shane Witbeck
 * @since 3/9/17
 */
public class AWSLocal implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(AWSLocal.class);

  private static final String SIGNING_REGION = "local";
  private static final int BRIDGE_SERVER_PORT = 8082;
  private static final int LAMBDA_SERVER_PORT = 8080;
  private static final String HOST = "localhost";
  
  private boolean kinesisStreamsEnabled;
  private int kinesisPort;
  private String kinesisEndpoint;
  
  private boolean dynamoDbEnabled;
  private int dynamoDbPort;
  private String dynamoDbEndpoint;

  private LambdaServiceType lambdaServiceType;

  private static LambdaServer lambdaServer;
  private static DockerBridgeServer dockerBridgeServer;
  private static DockerDynamoDB dockerDynamoDB;
  private static DockerKinesis dockerKinesis;
  
  public AWSLocal(Builder builder) {
    this.dynamoDbEnabled = builder.enableDynamoDB;
    this.kinesisStreamsEnabled = builder.enableKinesisStreams;
    this.lambdaServiceType = builder.lambdaServiceType;
  }
  
  public void start() {
    if (isDynamoDbEnabled()) {
      dockerDynamoDB = new DockerDynamoDB();
      this.dynamoDbPort = dockerDynamoDB.start();
      this.dynamoDbEndpoint = "http://" + HOST + ":" + dynamoDbPort;      
      log.info("DynamoDB endpoint: {}", this.dynamoDbEndpoint);
    }
    
    if (isKinesisStreamsEnabled()) {
      dockerKinesis = new DockerKinesis();
      this.kinesisPort = dockerKinesis.start();
      this.kinesisEndpoint = "http://" + HOST + ":" + kinesisPort;
      log.info("Kinesis Streams endpoint: {}", this.kinesisEndpoint);

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
    }));

    if (lambdaServiceType != null) {
      try {
        dockerBridgeServer = new DockerBridgeServer(BRIDGE_SERVER_PORT);
        dockerBridgeServer.start();
      } catch (Exception e) {
        log.error("Error starting BridgeServer", e);
      }

      if (LambdaServiceType.IN_MEMORY.equals(this.lambdaServiceType)) {
        lambdaServer = new LambdaServer(LAMBDA_SERVER_PORT,
            new InMemoryLambdaService(),
            new InMemoryEventSourceMappingService()
        );

      } else if (LambdaServiceType.FILESYSTEM.equals(this.lambdaServiceType)) {
        LocalFileSystemService localFileSystemService = new LocalFileSystemService();
        lambdaServer = new LambdaServer(LAMBDA_SERVER_PORT,
            new LocalFileLambdaService(localFileSystemService),
            new LocalFileEventSourceMappingService(localFileSystemService)
        );

      }

      try {
        lambdaServer.start();
      } catch (Exception e) {
        log.error("Error starting LambdaServer", e);
      }
    }
  }

  public LambdaServiceType getLambdaServiceType() {
    return lambdaServiceType;
  }

  public boolean isKinesisStreamsEnabled() {
    return kinesisStreamsEnabled;
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

  public static Builder builder() {
    return new Builder();
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
    private LambdaServiceType lambdaServiceType;
    
    public Builder enableKinesisStreams() {
      this.enableKinesisStreams = true;
      return this;
    }
    
    public Builder enableLambda(LambdaServiceType lambdaServiceType) {
      requireNonNull(lambdaServiceType, "LambdaServiceType is required");
      this.lambdaServiceType = lambdaServiceType;
      return this;
    }

    public Builder enableDynamoDB() {
      this.enableDynamoDB = true;
      return this;
    }
    
    public AWSLocal build() {
      return new AWSLocal(this);
    }
  }
}

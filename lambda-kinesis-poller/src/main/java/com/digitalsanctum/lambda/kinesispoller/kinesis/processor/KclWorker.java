package com.digitalsanctum.lambda.kinesispoller.kinesis.processor;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.digitalsanctum.lambda.kinesispoller.kinesis.config.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.digitalsanctum.lambda.kinesispoller.kinesis.config.ConfigurationUtils.getApplicationName;

/**
 * @author Shane Witbeck
 * @since 8/20/16
 */
public class KclWorker {
  private static final Logger log = LoggerFactory.getLogger(KclWorker.class);

  private final String workerId;
  private final Worker worker;

  public KclWorker(String streamName,
                   String kinesisEndpoint,
                   String dynamoDbEndpoint,
                   String lambdaServerEndpoint,
                   AWSCredentialsProvider credentialsProvider) {

    this.workerId = streamName + "-worker-" + UUID.randomUUID();
    log.info("initializing worker {}", this.workerId);

    KinesisClientLibConfiguration kclConfig
        = new KinesisClientLibConfiguration(getApplicationName(), streamName, credentialsProvider, this.workerId)
        .withKinesisEndpoint(kinesisEndpoint)
        .withCommonClientConfig(ConfigurationUtils.getClientConfigWithUserAgent());

    AmazonKinesis amazonKinesis = new AmazonKinesisClient(credentialsProvider);
    amazonKinesis.setEndpoint(kclConfig.getKinesisEndpoint());
    log.info("Kinesis client initialized with endpoint {}", kclConfig.getKinesisEndpoint());

    AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
    dynamoDBClient.setEndpoint(dynamoDbEndpoint);
    log.info("DynamoDB client initialized with endpoint {}", dynamoDbEndpoint);

    AmazonCloudWatch amazonCloudWatch = new AmazonCloudWatchClient(credentialsProvider);
    log.info("CloudWatch client initialized");

    this.worker = new Worker.Builder()
        .recordProcessorFactory(new RecordProcessorFactory(lambdaServerEndpoint))
        .config(kclConfig)
        .kinesisClient(amazonKinesis)
        .dynamoDBClient(dynamoDBClient)
        .cloudWatchClient(amazonCloudWatch)
        .build();
  }

  public String getWorkerId() {
    return workerId;
  }

  public void start() {
    try {
      log.info("starting worker {}", workerId);
      Thread workerThread = new Thread(worker, getWorkerId());
      workerThread.start();   
    } catch (Throwable t) {
      log.error("Caught throwable while processing data", t);
    }
    log.info("worker {} started", workerId);
  }

  public void stop() {
    log.info("stopping worker {}", workerId);
    worker.shutdown();
    log.info("worker {} stopped", workerId);
  }
}

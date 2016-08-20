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
 * @since 4/3/16
 */
public class KclWorker {

  private static final Logger log = LoggerFactory.getLogger(KclWorker.class);

  private final String workerId;
  private final Worker worker;

  public KclWorker(String streamName,
                   String kinesisEndpoint,
                   String dynamoDbEndpoint,
                   AWSCredentialsProvider credentialsProvider) {

    this.workerId = streamName + "-worker-" + UUID.randomUUID();

    KinesisClientLibConfiguration kclConfig
        = new KinesisClientLibConfiguration(getApplicationName(), streamName, credentialsProvider, this.workerId)
        .withKinesisEndpoint(kinesisEndpoint)
        .withCommonClientConfig(ConfigurationUtils.getClientConfigWithUserAgent());

    AmazonKinesis amazonKinesis = new AmazonKinesisClient(credentialsProvider);
    amazonKinesis.setEndpoint(kclConfig.getKinesisEndpoint());
    
    AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(credentialsProvider);
    dynamoDBClient.setEndpoint(dynamoDbEndpoint);

    AmazonCloudWatch amazonCloudWatch = new AmazonCloudWatchClient(credentialsProvider);

    this.worker = new Worker(new RecordProcessorFactory(), kclConfig, amazonKinesis, dynamoDBClient, amazonCloudWatch);
  }

  public String getWorkerId() {
    return workerId;
  }

  public void start() {
    try {
      log.info("starting worker");
      Thread workerThread = new Thread(worker, getWorkerId());
      workerThread.start();
//      workerThread.join();
//      worker.run();      
    } catch (Throwable t) {
      log.error("Caught throwable while processing data", t);
    }
    log.info("exiting");
  }

  public void stop() {
    worker.shutdown();
  }
}

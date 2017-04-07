package com.digitalsanctum.lambda.poller.dynamodb;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.streamsadapter.AmazonDynamoDBStreamsAdapterClient;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.digitalsanctum.lambda.lifecycle.AWSLocal.SIGNING_REGION;

/**
 * @author Shane Witbeck
 * @since 4/3/17
 */
public class DynamoDbStreamsWorker {

  private static final Logger log = LoggerFactory.getLogger(DynamoDbStreamsWorker.class);

  private static final String APPLICATION_NAME = "streams-adapter";
  private static final String WORKER_ID = "streams-adapter-worker";

  private Worker worker;

  public DynamoDbStreamsWorker(String dynamoDbStream,
                               String dynamoDbEndpoint,
                               AWSCredentialsProvider credentialsProvider,
                               AmazonDynamoDBStreamsAdapterClient amazonDynamoDBStreamsAdapterClient,
                               IRecordProcessorFactory recordProcessorFactory) {

    AwsClientBuilder.EndpointConfiguration dynamoDBEndpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration(dynamoDbEndpoint, SIGNING_REGION);
    AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClient.builder()
        .withCredentials(credentialsProvider)
        .withEndpointConfiguration(dynamoDBEndpointConfiguration)
        .build();

    AmazonCloudWatch amazonCloudWatch = AmazonCloudWatchClient.builder().withCredentials(credentialsProvider).build();

    KinesisClientLibConfiguration kinesisClientLibConfiguration
        = new KinesisClientLibConfiguration(APPLICATION_NAME, dynamoDbStream, credentialsProvider, WORKER_ID)
        .withMaxRecords(1000)
        .withIdleTimeBetweenReadsInMillis(500)
        .withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON);
    
    log.info("Creating worker for stream: {}", dynamoDbStream);
    worker = new Worker.Builder()
        .cloudWatchClient(amazonCloudWatch)
        .config(kinesisClientLibConfiguration)
        .dynamoDBClient(dynamoDBClient)        
        .kinesisClient(amazonDynamoDBStreamsAdapterClient)
        .recordProcessorFactory(recordProcessorFactory)
        .build();
  }

  public void start() {
    Thread t = new Thread(worker);
    t.start();
    log.info("Worker started");
  }

  public void stop() {
    worker.shutdown();    
    log.info("Worker stopped");
  }
}

/*
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.dynamodbv2.streamsadapter;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreamsClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.amazonaws.services.kinesis.metrics.interfaces.IMetricsFactory;

import java.util.concurrent.ExecutorService;

/**
 * The StreamsWorker extends the Kinesis Client Library's Worker
 * class to provide convenient constructors for ease-of-use.
 */
public class StreamsWorker extends Worker {

  /**
   * Constructor.
   *
   * @param recordProcessorFactory Used to get record processor instances for processing data from shards
   * @param config                 Kinesis Client Library configuration
   */
  public StreamsWorker(IRecordProcessorFactory recordProcessorFactory,
                       KinesisClientLibConfiguration config) {
    this(recordProcessorFactory, config,
        new AmazonDynamoDBStreamsAdapterClient(AmazonDynamoDBStreamsClientBuilder.standard()
            .withClientConfiguration(config.getKinesisClientConfiguration())
            .withCredentials(config.getKinesisCredentialsProvider())
            .build()),
        AmazonDynamoDBClientBuilder.standard()
            .withCredentials(config.getDynamoDBCredentialsProvider())
            .withClientConfiguration(config.getDynamoDBClientConfiguration())
            .build(),
        AmazonCloudWatchClientBuilder.standard()
            .withClientConfiguration(config.getCloudWatchClientConfiguration())
            .withCredentials(config.getCloudWatchCredentialsProvider())
            .build()
    );
  }

  /**
   * @param recordProcessorFactory Used to get record processor instances for processing data from shards
   * @param config                 Kinesis Client Library configuration
   * @param streamsClient          DynamoDB Streams Adapter Client used for fetching data
   * @param dynamoDBClient         DynamoDB client used for checkpoints and tracking leases
   * @param cloudWatchClient       CloudWatch Client for publishing metrics
   */
  public StreamsWorker(IRecordProcessorFactory recordProcessorFactory,
                       KinesisClientLibConfiguration config,
                       AmazonKinesis streamsClient,
                       AmazonDynamoDB dynamoDBClient,
                       AmazonCloudWatch cloudWatchClient) {
    super(recordProcessorFactory, config, streamsClient, dynamoDBClient, cloudWatchClient);
  }

  /**
   * @param recordProcessorFactory Used to get record processor instances for processing data from shards
   * @param config                 Kinesis Client Library configuration
   * @param streamsClient          DynamoDB Streams Adapter Client used for fetching data
   * @param dynamoDBClient         DynamoDB client used for checkpoints and tracking leases
   * @param cloudWatchClient       CloudWatch Client for publishing metrics
   * @param execService            ExecutorService to use for processing records (support for multi-threaded
   *                               consumption)
   */
  public StreamsWorker(IRecordProcessorFactory recordProcessorFactory,
                       KinesisClientLibConfiguration config,
                       AmazonKinesis streamsClient,
                       AmazonDynamoDB dynamoDBClient,
                       AmazonCloudWatch cloudWatchClient,
                       ExecutorService execService) {
    super(recordProcessorFactory, config, streamsClient, dynamoDBClient,
        cloudWatchClient, execService);
  }

  /**
   * @param recordProcessorFactory Used to get record processor instances for processing data from shards
   * @param config                 Kinesis Client Library configuration
   * @param streamsClient          DynamoDB Streams Adapter Client used for fetching data
   * @param dynamoDBClient         DynamoDB client used for checkpoints and tracking leases
   * @param metricsFactory         Metrics factory used to emit metrics
   * @param execService            ExecutorService to use for processing records (support for multi-threaded
   *                               consumption)
   */
  public StreamsWorker(IRecordProcessorFactory recordProcessorFactory,
                       KinesisClientLibConfiguration config,
                       AmazonKinesis streamsClient,
                       AmazonDynamoDB dynamoDBClient,
                       IMetricsFactory metricsFactory,
                       ExecutorService execService) {
    super(recordProcessorFactory, config, streamsClient, dynamoDBClient,
        metricsFactory, execService);
  }

  /**
   * @param recordProcessorFactory Used to get record processor instances for processing data from shards
   * @param config                 Kinesis Client Library configuration
   * @param streamsClient          DynamoDB Streams Adapter Client used for fetching data
   * @param dynamoDBClient         DynamoDB client used for checkpoints and tracking leases
   * @param cloudWatchClient       CloudWatch Client for publishing metrics
   */
  public StreamsWorker(IRecordProcessorFactory recordProcessorFactory,
                       KinesisClientLibConfiguration config,
                       AmazonKinesisClient streamsClient,
                       AmazonDynamoDBClient dynamoDBClient,
                       AmazonCloudWatchClient cloudWatchClient) {
    super(recordProcessorFactory, config, streamsClient, dynamoDBClient,
        cloudWatchClient);
  }

  /**
   * @param recordProcessorFactory Used to get record processor instances for processing data from shards
   * @param config                 Kinesis Client Library configuration
   * @param streamsClient          DynamoDB Streams Adapter Client used for fetching data
   * @param dynamoDBClient         DynamoDB client used for checkpoints and tracking leases
   * @param cloudWatchClient       CloudWatch Client for publishing metrics
   * @param execService            ExecutorService to use for processing records (support for multi-threaded
   *                               consumption)
   */
  public StreamsWorker(IRecordProcessorFactory recordProcessorFactory,
                       KinesisClientLibConfiguration config,
                       AmazonKinesisClient streamsClient,
                       AmazonDynamoDBClient dynamoDBClient,
                       AmazonCloudWatchClient cloudWatchClient,
                       ExecutorService execService) {
    super(recordProcessorFactory, config, streamsClient, dynamoDBClient,
        cloudWatchClient, execService);
  }

  /**
   * @param recordProcessorFactory Used to get record processor instances for processing data from shards
   * @param config                 Kinesis Client Library configuration
   * @param streamsClient          DynamoDB Streams Adapter Client used for fetching data
   * @param dynamoDBClient         DynamoDB client used for checkpoints and tracking leases
   * @param metricsFactory         Metrics factory used to emit metrics
   * @param execService            ExecutorService to use for processing records (support for multi-threaded
   *                               consumption)
   */
  public StreamsWorker(IRecordProcessorFactory recordProcessorFactory,
                       KinesisClientLibConfiguration config,
                       AmazonKinesisClient streamsClient,
                       AmazonDynamoDBClient dynamoDBClient,
                       IMetricsFactory metricsFactory,
                       ExecutorService execService) {
    super(recordProcessorFactory, config, streamsClient, dynamoDBClient,
        metricsFactory, execService);
  }

}

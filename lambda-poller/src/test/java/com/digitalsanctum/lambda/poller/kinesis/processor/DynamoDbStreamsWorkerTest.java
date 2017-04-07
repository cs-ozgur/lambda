package com.digitalsanctum.lambda.kinesispoller.kinesis.processor;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreamsClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;
import com.amazonaws.services.dynamodbv2.model.StreamViewType;
import com.amazonaws.services.dynamodbv2.streamsadapter.AmazonDynamoDBStreamsAdapterClient;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.digitalsanctum.lambda.lifecycle.AWSLocal;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.digitalsanctum.lambda.lifecycle.AWSLocal.SIGNING_REGION;
import static com.google.common.collect.ImmutableMap.of;

/**
 * @author Shane Witbeck
 * @since 4/4/17
 */
public class DynamoDbStreamsWorkerTest {

  private static final Logger log = LoggerFactory.getLogger(DynamoDbStreamsWorkerTest.class);

  private static final String HASH_KEY_ATTRIBUTE_NAME = "id";
  private static final String TABLE_NAME = "foo";
  private static final long RCU = 1L, WCU = 1L;

  private static AmazonDynamoDB amazonDynamoDB;
  private static DynamoDbStreamsWorker dynamoDbStreamsWorker;

  @BeforeClass
  public static void setupClazz() throws Exception {
    
    AWSLocal awsLocal = AWSLocal.builder()
        .enableDynamoDB()
        .enableLambda(AWSLocal.LambdaServiceType.FILESYSTEM)
        .build();
    awsLocal.start();

    String dynamoDbEndpoint = awsLocal.getDynamoDbEndpoint();

    AWSCredentialsProvider credentialsProvider = new DefaultAWSCredentialsProviderChain();
    
    // create DynamoDB client
    AwsClientBuilder.EndpointConfiguration dynamoDbEndpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration(dynamoDbEndpoint, SIGNING_REGION);
    amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(dynamoDbEndpointConfiguration)
        .build();

    // create DynamoDB streams client
    AmazonDynamoDBStreams amazonDynamoDBStreams = AmazonDynamoDBStreamsClient.builder()
        .withEndpointConfiguration(dynamoDbEndpointConfiguration)
        .build();

    // create a DynamoDB table with streams enabled
    KeySchemaElement keySchemaElement = new KeySchemaElement(HASH_KEY_ATTRIBUTE_NAME, KeyType.HASH);
    AttributeDefinition attributeDefinition = new AttributeDefinition(HASH_KEY_ATTRIBUTE_NAME, ScalarAttributeType.S);
    ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput(RCU, WCU);
    CreateTableRequest createTableRequest = new CreateTableRequest()
        .withTableName(TABLE_NAME)
        .withStreamSpecification(new StreamSpecification()
            .withStreamEnabled(true)
            .withStreamViewType(StreamViewType.NEW_AND_OLD_IMAGES))
        .withKeySchema(keySchemaElement)
        .withAttributeDefinitions(attributeDefinition)
        .withProvisionedThroughput(provisionedThroughput);
    CreateTableResult createTableResult = amazonDynamoDB.createTable(createTableRequest);
    String latestStreamArn = createTableResult.getTableDescription().getLatestStreamArn();
    log.info(latestStreamArn);

    // create DynamoDB streams adapter client    
    AmazonDynamoDBStreamsAdapterClient adapterClient = new AmazonDynamoDBStreamsAdapterClient(amazonDynamoDBStreams);

    IRecordProcessorFactory recordProcessorFactory = new SimpleProcessorFactory();
        
    dynamoDbStreamsWorker = new DynamoDbStreamsWorker(latestStreamArn, dynamoDbEndpoint, credentialsProvider,
        adapterClient, recordProcessorFactory);
  }

  @Test
  public void test() throws Exception {
    dynamoDbStreamsWorker.start();

    Thread.sleep(5_000);
    
    // put a record to DynamoDB which should trigger an invocation of the lambda function
    String hashKeyValue = UUID.randomUUID().toString();
    log.info("test value: {}", hashKeyValue);
    PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME, of(HASH_KEY_ATTRIBUTE_NAME, new AttributeValue().withS(hashKeyValue)));
    PutItemResult putItemResult = amazonDynamoDB.putItem(putItemRequest);
    log.info("putItemResult: {}", putItemResult.toString());

    Thread.sleep(10_000);

    dynamoDbStreamsWorker.stop();
  }
}

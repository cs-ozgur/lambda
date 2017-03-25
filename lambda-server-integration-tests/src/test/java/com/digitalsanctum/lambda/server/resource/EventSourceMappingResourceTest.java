package com.digitalsanctum.lambda.server.resource;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.protocol.json.SdkJsonGenerator;
import com.amazonaws.protocol.json.StructuredJsonGenerator;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreams;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreamsClient;
import com.amazonaws.services.dynamodbv2.local.shared.access.LocalDynamoDBStreamsClient;
import com.amazonaws.services.dynamodbv2.local.shared.access.sqlite.SQLiteDBAccess;
import com.amazonaws.services.dynamodbv2.local.shared.jobs.JobsRegister;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.util.IOUtils;
import com.digitalsanctum.lambda.marshallers.DynamodbEventJsonMarshaller;
import com.digitalsanctum.lambda.server.LocalBaseTest;
import com.digitalsanctum.lambda.service.localfile.LocalFileSystemService;
import com.fasterxml.jackson.core.JsonFactory;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

import static com.amazonaws.services.lambda.model.EventSourcePosition.TRIM_HORIZON;
import static com.digitalsanctum.lambda.server.resource.FunctionResourceTest.TEST_LAMBDA_JAR;
import static com.digitalsanctum.lambda.server.resource.FunctionResourceTest.TEST_RUNTIME;
import static com.digitalsanctum.lambda.service.localfile.LocalFileEventSourceMappingService.MAPPING_SUFFIX;
import static com.digitalsanctum.lambda.service.localfile.LocalFileEventSourceMappingService.ROOT_DIR;
import static com.google.common.collect.ImmutableMap.of;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 3/9/17
 */
public class EventSourceMappingResourceTest extends LocalBaseTest {

  private static final Logger log = LoggerFactory.getLogger(EventSourceMappingResourceTest.class);

  private static final String TABLE_NAME = "foo";
  private static final String FUNCTION_NAME = "basicdynamodb";
  private static final String FUNCTION_HANDLER = "com.digitalsanctum.lambda.functions.event.dynamodb.BasicDynamoDb::handler";
  private static final long RCU = 1L, WCU = 1L;

  private LocalFileSystemService localFileSystemService;
  private AmazonDynamoDBStreams amazonDynamoDBStreams;
  private AmazonDynamoDB amazonDynamoDB;

  @Before
  public void setup() throws Exception {
    super.setup();

    // instantiate DynamoDB client to point to local DynamoDB
    String dynamoDbEndpoint = awsLocal.getDynamoDbEndpoint();
    EndpointConfiguration dynamoDbEndpointConfiguration = new EndpointConfiguration(dynamoDbEndpoint, "local");
    amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(dynamoDbEndpointConfiguration)
        .build();

    amazonDynamoDBStreams = AmazonDynamoDBStreamsClient.builder()
        .withEndpointConfiguration(dynamoDbEndpointConfiguration)
        .build();
    
    localFileSystemService = new LocalFileSystemService();
    deleteAllEventSourceMappings();
  }

  @After
  public void afterTest() throws Exception {
//    deleteAllEventSourceMappings();
  }

  @Test
//  @Ignore
  public void createEventSourceMapping() throws Exception {

    // instantiate DynamoDB client to point to local DynamoDB
    String dynamoDbEndpoint = awsLocal.getDynamoDbEndpoint();
    EndpointConfiguration dynamoDbEndpointConfiguration = new EndpointConfiguration(dynamoDbEndpoint, "local");

    // create a DynamoDB table
    String hashKeyAttributeName = "id";
    KeySchemaElement keySchemaElement = new KeySchemaElement(hashKeyAttributeName, KeyType.HASH);
    AttributeDefinition attributeDefinition = new AttributeDefinition(hashKeyAttributeName, ScalarAttributeType.S);
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

    // create function code from local test jar 
    InputStream is = FunctionResourceTest.class.getResourceAsStream(TEST_LAMBDA_JAR);
    byte[] lambdaByteArr = IOUtils.toByteArray(is);
    ByteBuffer functionZip = ByteBuffer.wrap(lambdaByteArr);
    FunctionCode functionCode = new FunctionCode().withZipFile(functionZip);

    // create function
    CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
        .withFunctionName(FUNCTION_NAME) // must be lowercase since we're using this as the docker repository name
        .withCode(functionCode)
        .withRuntime(TEST_RUNTIME)
        .withPublish(true)
        .withHandler(FUNCTION_HANDLER);

    CreateFunctionResult createFunctionResult = awsLambda.createFunction(createFunctionRequest);
    assertNotNull(createFunctionResult);
    assertEquals(FUNCTION_NAME, createFunctionResult.getFunctionName());
    assertEquals("arn:aws:lambda:local:111000111000:function:" + FUNCTION_NAME, createFunctionResult.getFunctionArn());

    // HACK: invoke the function so function container actually runs    
    String payload = getTestDynamodbEvent();
    awsLambda.invoke(new InvokeRequest()
        .withFunctionName(FUNCTION_NAME)
        .withPayload(payload)
        .withInvocationType(InvocationType.Event));

    // create mapping
    CreateEventSourceMappingRequest createEventSourceMappingRequest = new CreateEventSourceMappingRequest()
        .withEnabled(true)
        .withFunctionName(createFunctionResult.getFunctionName())
        .withStartingPosition(TRIM_HORIZON)
        .withBatchSize(100)
        .withEventSourceArn(latestStreamArn);

    // TODO create poller to consume from DynamoDB stream and invoke lambda
    // see https://github.com/awslabs/dynamodb-streams-kinesis-adapter ?

    CreateEventSourceMappingResult createEventSourceMappingResult = awsLambda.createEventSourceMapping(createEventSourceMappingRequest);
    log.info(createEventSourceMappingResult.toString());

    // put a record to DynamoDB which should trigger an invocation of the lambda function
    String hashKeyValue = UUID.randomUUID().toString();
    log.info("test value: {}", hashKeyValue);
    PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME, of(hashKeyAttributeName, new AttributeValue().withS(hashKeyValue)));
    PutItemResult putItemResult = amazonDynamoDB.putItem(putItemRequest);
    log.info("putItemResult: {}", putItemResult.toString());

    ListStreamsRequest listStreamsRequest = new ListStreamsRequest().withTableName(TABLE_NAME);
    ListStreamsResult listStreamsResult = amazonDynamoDBStreams.listStreams(listStreamsRequest);
    List<Stream> streams = listStreamsResult.getStreams();
    streams.forEach(s -> log.info("stream: {}", s.toString()));

    if (streams.isEmpty()) {
      log.error("no streams found");
    }

    String streamArn = streams.get(0).getStreamArn();

    DescribeStreamRequest describeStreamRequest = new DescribeStreamRequest().withStreamArn(streamArn);
    DescribeStreamResult describeStreamResult = amazonDynamoDBStreams.describeStream(describeStreamRequest);    
    describeStreamResult.getStreamDescription().getShards().forEach(shard -> {
      GetShardIteratorResult getShardIteratorResult = amazonDynamoDBStreams.getShardIterator(new GetShardIteratorRequest()
          .withShardIteratorType(ShardIteratorType.TRIM_HORIZON)
          .withShardId(shard.getShardId())
          .withStreamArn(streamArn));

      String shardIterator = getShardIteratorResult.getShardIterator();

      GetRecordsRequest getRecordsRequest = new GetRecordsRequest().withShardIterator(shardIterator).withLimit(10);
      GetRecordsResult getRecordsResult = amazonDynamoDBStreams.getRecords(getRecordsRequest);

      getRecordsResult.getRecords().forEach(record -> log.info("record: {}", record.toString()));
    });
  }

  private String getTestDynamodbEvent() {

    StreamRecord streamRecord = new StreamRecord()
        .withStreamViewType("NEW_AND_OLD_IMAGES")
        .withSequenceNumber("111")
        .withSizeBytes(26L)
        .withApproximateCreationDateTime(new Date(1485566940000L))
        .withKeys(of(
            "Id", new AttributeValue().withN("101")
        ))
        .withNewImage(of(
            "Message", new AttributeValue("New item!"),
            "Id", new AttributeValue().withN("101")
        ));

    DynamodbEvent.DynamodbStreamRecord record = new DynamodbEvent.DynamodbStreamRecord();
    record.setEventSourceARN("arn:aws:dynamodb:us-west-2:account-id:table/ExampleTableWithStream/stream/2015-06-27T00:48:05.899");
    record.setAwsRegion("us-west-2");
    record.setDynamodb(streamRecord);
    record.setEventID("1");
    record.setEventName("INSERT");
    record.setEventSource("aws:dynamodb");
    record.setEventVersion("1.0");

    DynamodbEvent event = new DynamodbEvent();
    event.setRecords(ImmutableList.of(record));

    JsonFactory jsonFactory = new JsonFactory();
    StructuredJsonGenerator structuredJsonGenerator = new SdkJsonGenerator(jsonFactory, "application/json");

    DynamodbEventJsonMarshaller.getInstance().marshall(event, structuredJsonGenerator);

    return new String(structuredJsonGenerator.getBytes());
  }
  
  /*
  {
      "eventID": "1",
      "eventVersion": "1.0",
      "dynamodb": {
        "Keys": {
          "Id": {
            "N": "101"
          }
        },
        "NewImage": {
          "Message": {
            "S": "New item!"
          },
          "Id": {
            "N": "101"
          }
        },
        "StreamViewType": "NEW_AND_OLD_IMAGES",
        "SequenceNumber": "111",
        "SizeBytes": 26
      },
      "awsRegion": "us-west-2",
      "eventName": "INSERT",
      "eventSourceARN": "arn:aws:dynamodb:us-west-2:account-id:table/ExampleTableWithStream/stream/2015-06-27T00:48:05.899",
      "eventSource": "aws:dynamodb"
    }
   */

  @Test
  @Ignore
  public void listEventSourceMappings() throws Exception {
    ListEventSourceMappingsResult result = awsLambda.listEventSourceMappings();
    assertThat(result.getEventSourceMappings().size(), is(0));
  }

  private void deleteAllEventSourceMappings() throws Exception {
    PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + ROOT_DIR + "**/*" + MAPPING_SUFFIX);
    try {
      Files.walk(ROOT_DIR)
          .filter(pathMatcher::matches)
          .forEach(path -> localFileSystemService.delete(path));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

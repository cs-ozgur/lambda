package com.digitalsanctum.lambda.server.resource;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.CreateTableResult;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.StreamSpecification;
import com.amazonaws.services.dynamodbv2.model.StreamViewType;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.DeleteEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.DeleteFunctionRequest;
import com.amazonaws.services.lambda.model.EventSourcePosition;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsAsyncClientBuilder;
import com.amazonaws.services.logs.model.DeleteLogGroupRequest;
import com.amazonaws.services.logs.model.DescribeLogStreamsRequest;
import com.amazonaws.services.logs.model.DescribeLogStreamsResult;
import com.amazonaws.services.logs.model.FilterLogEventsRequest;
import com.amazonaws.services.logs.model.FilterLogEventsResult;
import com.amazonaws.services.logs.model.FilteredLogEvent;
import com.amazonaws.services.logs.model.GetLogEventsRequest;
import com.amazonaws.services.logs.model.GetLogEventsResult;
import com.amazonaws.services.logs.model.LogStream;
import com.amazonaws.services.logs.model.ResourceNotFoundException;
import com.amazonaws.util.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.digitalsanctum.lambda.server.resource.FunctionResourceTest.TEST_LAMBDA_JAR;
import static com.digitalsanctum.lambda.server.resource.FunctionResourceTest.TEST_RUNTIME;
import static com.google.common.collect.ImmutableMap.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Shane Witbeck
 * @since 3/17/17
 */
public class AWSEventSourceMappingTest {

  private static final Logger log = LoggerFactory.getLogger(EventSourceMappingResourceTest.class);

  private static final String IAM_ROLE = "arn:aws:iam::515292396565:role/lambda-test-role";
  private static final String TABLE_NAME = "foo";
  private static final String FUNCTION_NAME = "basicdynamodb";
  private static final String FUNCTION_HANDLER = "com.digitalsanctum.lambda.functions.event.dynamodb.BasicDynamoDb::handler";

  private AmazonDynamoDB amazonDynamoDB;
  private AWSLambda awsLambda;
  private AWSLogs awsLogs;

  private String eventSourceMappingUUID;
  private String logGroupName;


  @Before
  public void setup() throws Exception {

    String lambdaLogGroupNamePrefix = "/aws/lambda/";
    logGroupName = lambdaLogGroupNamePrefix + FUNCTION_NAME;

    AWSCredentialsProvider awsCredentialsProvider = new ProfileCredentialsProvider("shane");

    amazonDynamoDB = AmazonDynamoDBClientBuilder.standard().withCredentials(awsCredentialsProvider).build();
    awsLambda = AWSLambdaClientBuilder.standard().withCredentials(awsCredentialsProvider).build();
    awsLogs = AWSLogsAsyncClientBuilder.standard().withCredentials(awsCredentialsProvider).build();
  }

  @After
  public void afterTest() throws Exception {
    // TODO if exists checks and block until complete
    if (eventSourceMappingUUID != null) {
      awsLambda.deleteEventSourceMapping(new DeleteEventSourceMappingRequest().withUUID(eventSourceMappingUUID));
    }
    amazonDynamoDB.deleteTable(TABLE_NAME);
    awsLambda.deleteFunction(new DeleteFunctionRequest().withFunctionName(FUNCTION_NAME));
    awsLogs.deleteLogGroup(new DeleteLogGroupRequest(logGroupName));
  }

  @Test
  @Ignore
  public void testLogs() {

    String lambdaLogGroupNamePrefix = "/aws/lambda/";
    String logGroupName = lambdaLogGroupNamePrefix + FUNCTION_NAME;

    DescribeLogStreamsRequest describeLogStreamsRequest = new DescribeLogStreamsRequest(logGroupName);
    DescribeLogStreamsResult describeLogStreamsResult = awsLogs.describeLogStreams(describeLogStreamsRequest);
    List<LogStream> logStreams = describeLogStreamsResult.getLogStreams();

    describeLogStreamsResult.getLogStreams().forEach(logStream -> System.out.println(logStream.toString()));

    String logStreamName = logStreams.get(0).getLogStreamName();
    GetLogEventsRequest getLogEventsRequest = new GetLogEventsRequest(logGroupName, logStreamName);
    GetLogEventsResult result = awsLogs.getLogEvents(getLogEventsRequest);

    result.getEvents().forEach(outputLogEvent -> System.out.println(outputLogEvent.toString()));

  }

  @Test
  @Ignore
  public void createEventSourceMapping() throws Exception {

    // create a DynamoDB table
    String hashKeyAttributeName = "id";
    KeySchemaElement keySchemaElement = new KeySchemaElement(hashKeyAttributeName, KeyType.HASH);
    AttributeDefinition attributeDefinition = new AttributeDefinition(hashKeyAttributeName, ScalarAttributeType.S);
    long rcu = 1L, wcu = 1L;
    ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput(rcu, wcu);
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
    FunctionCode functionCode = new FunctionCode()
        .withZipFile(functionZip);

    // create function
    CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
        .withFunctionName(FUNCTION_NAME) // must be lowercase since we're using this as the docker repository name
        .withCode(functionCode)
        .withRuntime(TEST_RUNTIME)
        .withRole(IAM_ROLE)
        .withPublish(true)
        .withHandler(FUNCTION_HANDLER);

    CreateFunctionResult createFunctionResult = awsLambda.createFunction(createFunctionRequest);
    assertNotNull(createFunctionResult);
    assertEquals(FUNCTION_NAME, createFunctionResult.getFunctionName());

    // create mapping
    CreateEventSourceMappingRequest createEventSourceMappingRequest = new CreateEventSourceMappingRequest()
        .withEnabled(true)
        .withFunctionName(FUNCTION_NAME)
        .withStartingPosition(EventSourcePosition.TRIM_HORIZON)
        .withEventSourceArn(latestStreamArn);

    CreateEventSourceMappingResult createEventSourceMappingResult = awsLambda.createEventSourceMapping(createEventSourceMappingRequest);
    log.info(createEventSourceMappingResult.toString());
    eventSourceMappingUUID = createEventSourceMappingResult.getUUID();

    // put a record to DynamoDB which should trigger an invocation of the lambda function
    String hashKeyValue = UUID.randomUUID().toString();
    PutItemRequest putItemRequest = new PutItemRequest(TABLE_NAME, of(hashKeyAttributeName, new AttributeValue().withS(hashKeyValue)));
    PutItemResult putItemResult = amazonDynamoDB.putItem(putItemRequest);
    log.info(putItemResult.toString());

    GetItemResult getItemResult = amazonDynamoDB.getItem(new GetItemRequest(TABLE_NAME, of(hashKeyAttributeName, new AttributeValue(hashKeyValue))));
    log.info(getItemResult.toString());

    // inspect function logs to verify the new DynamoDB record shows up
    String lambdaLogGroupNamePrefix = "/aws/lambda/";
    String logGroupName = lambdaLogGroupNamePrefix + FUNCTION_NAME;

    DescribeLogStreamsRequest describeLogStreamsRequest = new DescribeLogStreamsRequest(logGroupName);
    boolean found = false;
    while (!found) {
      try {
        DescribeLogStreamsResult describeLogStreamsResult = awsLogs.describeLogStreams(describeLogStreamsRequest);
        /*List<LogStream> logStreams = describeLogStreamsResult.getLogStreams();
        describeLogStreamsResult.getLogStreams().forEach(logStream -> System.out.println(logStream.toString()));
        String logStreamName = logStreams.get(0).getLogStreamName();*/
        found = true;
      } catch (ResourceNotFoundException nf) {
        log.info("Logs not found yet; waiting 5s...");
        Thread.sleep(5000);
      }
    }

    boolean foundHashKeyValue = searchLogs(logGroupName, "received");
  }

  private boolean searchLogs(String logGroupName, String filterPattern, String... logStreamNames) {
    FilterLogEventsRequest filterLogEventsRequest = new FilterLogEventsRequest()
        .withLogGroupName(logGroupName)
        .withFilterPattern(filterPattern);
    if (logStreamNames != null && logStreamNames.length > 0) {
      filterLogEventsRequest.setLogStreamNames(Arrays.asList(logStreamNames));
    }
    boolean found = false;
    while(!found) {
      FilterLogEventsResult filterLogEventsResult = awsLogs.filterLogEvents(filterLogEventsRequest);
      List<FilteredLogEvent> filteredLogEvents = filterLogEventsResult.getEvents();
      if (filteredLogEvents.isEmpty()) {
        log.info("{} not yet found in {}", filterPattern, logGroupName);
        try {
          Thread.sleep(5000);
        } catch (InterruptedException ie) {
          // no op
        }
      } else {
        found = true;
      }      
    }
    return true;
  }
}

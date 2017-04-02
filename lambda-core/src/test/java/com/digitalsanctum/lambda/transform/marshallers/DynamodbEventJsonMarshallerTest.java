package com.digitalsanctum.lambda.transform.marshallers;

import com.amazonaws.Request;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.protocol.OperationInfo;
import com.amazonaws.protocol.Protocol;
import com.amazonaws.protocol.ProtocolRequestMarshaller;
import com.amazonaws.protocol.json.JsonClientMetadata;
import com.amazonaws.protocol.json.JsonErrorShapeMetadata;
import com.amazonaws.protocol.json.SdkJsonProtocolFactory;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.digitalsanctum.lambda.transform.JsonUnmarshallerContextFactory;
import com.digitalsanctum.lambda.transform.unmarshallers.DynamodbEventUnmarshaller;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 3/26/17
 */
public class DynamodbEventJsonMarshallerTest {

  private static final JsonClientMetadata JSON_CLIENT_METADATA = new JsonClientMetadata()
      .withProtocolVersion("1.0")
      .withSupportsCbor(false)
      .withSupportsIon(false)
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("TrimmedDataAccessException").withModeledClass(
              com.amazonaws.services.dynamodbv2.model.TrimmedDataAccessException.class))
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("ResourceNotFoundException").withModeledClass(
              com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException.class))
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("ExpiredIteratorException").withModeledClass(
              com.amazonaws.services.dynamodbv2.model.ExpiredIteratorException.class))
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("InternalServerError").withModeledClass(
              com.amazonaws.services.dynamodbv2.model.InternalServerErrorException.class))
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("LimitExceededException").withModeledClass(
              com.amazonaws.services.dynamodbv2.model.LimitExceededException.class))
      .withBaseServiceExceptionClass(com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException.class);

  private static final OperationInfo SDK_OPERATION_BINDING = OperationInfo.builder()
      .protocol(Protocol.AWS_JSON)
      .requestUri("/")
      .httpMethodName(HttpMethodName.POST)
      .hasExplicitPayloadMember(false)
      .hasPayloadMembers(true)
      .operationIdentifier("DynamoDB_20120810.PutDynamodbEvent") // best guess
      .serviceName("AmazonDynamoDBv2")
      .build();


  @Test
  public void testMarshalDynamodbEvent() throws Exception {

    Map<String, AttributeValue> keys = new HashMap<>();
    keys.put("Id", new AttributeValue().withN("101"));

    Map<String, AttributeValue> newImage = new HashMap<>();
    newImage.put("Message", new AttributeValue("New item!"));
    newImage.put("Id", new AttributeValue().withN("101"));

    StreamRecord streamRecord = new StreamRecord()
        .withStreamViewType("NEW_AND_OLD_IMAGES")
        .withSequenceNumber("111")
        .withSizeBytes(26L)
        .withApproximateCreationDateTime(new Date(1485566940000L))
        .withKeys(keys)
        .withNewImage(newImage);

    DynamodbEvent.DynamodbStreamRecord record = new DynamodbEvent.DynamodbStreamRecord();
    record.setEventSourceARN("arn:aws:dynamodb:us-west-2:account-id:table/ExampleTableWithStream/stream/2015-06-27T00:48:05.899");
    record.setAwsRegion("us-west-2");
    record.setDynamodb(streamRecord);
    record.setEventID("1");
    record.setEventName("INSERT");
    record.setEventSource("aws:dynamodb");
    record.setEventVersion("1.0");

    List<DynamodbEvent.DynamodbStreamRecord> records = new ArrayList<>();
    records.add(record);

    DynamodbEvent originalDynamodbEvent = new DynamodbEvent();
    originalDynamodbEvent.setRecords(records);

    // marshal

    SdkJsonProtocolFactory sdkJsonProtocolFactory = new SdkJsonProtocolFactory(JSON_CLIENT_METADATA);
    ProtocolRequestMarshaller protocolRequestMarshaller
        = sdkJsonProtocolFactory.createProtocolMarshaller(SDK_OPERATION_BINDING, null);

    protocolRequestMarshaller.startMarshalling();

    DynamodbEventJsonMarshaller.getInstance().marshall(originalDynamodbEvent, protocolRequestMarshaller);

    Request request = protocolRequestMarshaller.finishMarshalling();

    // unmarshal

    InputStream inputJson = request.getContent();
    JsonUnmarshallerContext jsonUnmarshallerContext = new JsonUnmarshallerContextFactory().getJsonUnmarshallerContext(inputJson);
    Object unmarshalledObject = DynamodbEventUnmarshaller.getInstance().unmarshall(jsonUnmarshallerContext);

    // assertions

    assertThat(unmarshalledObject.getClass().getName(), is(DynamodbEvent.class.getName()));

    DynamodbEvent unmarshalledEvent = (DynamodbEvent) unmarshalledObject;
    assertThat(unmarshalledEvent.getRecords().size(), is(originalDynamodbEvent.getRecords().size()));
    assertThat(unmarshalledEvent.getRecords().get(0).getDynamodb().getClass().getName(),
        is(originalDynamodbEvent.getRecords().get(0).getDynamodb().getClass().getName()));
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


}

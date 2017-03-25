package com.digitalsanctum.lambda.unmarshallers;

import com.amazonaws.services.dynamodbv2.model.transform.StreamRecordJsonUnmarshaller;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;
import com.fasterxml.jackson.core.JsonToken;

/**
 * @author Shane Witbeck
 * @since 3/11/17
 */
public class DynamodbStreamRecordUnmarshaller implements Unmarshaller<DynamodbEvent.DynamodbStreamRecord, JsonUnmarshallerContext> {

  private static DynamodbStreamRecordUnmarshaller instance;

  public DynamodbStreamRecordUnmarshaller() {
  }

  public DynamodbEvent.DynamodbStreamRecord unmarshall(JsonUnmarshallerContext context) throws Exception {
    DynamodbEvent.DynamodbStreamRecord dynamodbStreamRecord = new DynamodbEvent.DynamodbStreamRecord();
    int originalDepth = context.getCurrentDepth();
    String currentParentElement = context.getCurrentParentElement();
    int targetDepth = originalDepth + 1;
    JsonToken token = context.getCurrentToken();
    if (token == null) {
      token = context.nextToken();
    }

    if (token == JsonToken.VALUE_NULL) {
      return null;
    } else {
      for (; token != null; token = context.nextToken()) {
        if (token != JsonToken.FIELD_NAME && token != JsonToken.START_OBJECT) {
          if ((token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) && (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) && context.getCurrentDepth() <= originalDepth) {
            break;
          }
        } else {
          if (context.testExpression("eventID", targetDepth)) {
            context.nextToken();
            dynamodbStreamRecord.setEventID((String) context.getUnmarshaller(String.class).unmarshall(context));
          }

          if (context.testExpression("eventName", targetDepth)) {
            context.nextToken();
            dynamodbStreamRecord.setEventName((String) context.getUnmarshaller(String.class).unmarshall(context));
          }

          if (context.testExpression("eventVersion", targetDepth)) {
            context.nextToken();
            dynamodbStreamRecord.setEventVersion((String) context.getUnmarshaller(String.class).unmarshall(context));
          }

          if (context.testExpression("eventSource", targetDepth)) {
            context.nextToken();
            dynamodbStreamRecord.setEventSource((String) context.getUnmarshaller(String.class).unmarshall(context));
          }

          if (context.testExpression("awsRegion", targetDepth)) {
            context.nextToken();
            dynamodbStreamRecord.setAwsRegion((String) context.getUnmarshaller(String.class).unmarshall(context));
          }

          if (context.testExpression("dynamodb", targetDepth)) {
            context.nextToken();
            dynamodbStreamRecord.setDynamodb(StreamRecordJsonUnmarshaller.getInstance().unmarshall(context));
          }
          //eventSourceARN
          if (context.testExpression("eventSourceARN", targetDepth)) {
            context.nextToken();
            dynamodbStreamRecord.setEventSourceARN((String) context.getUnmarshaller(String.class).unmarshall(context));
          }
        }
      }

      return dynamodbStreamRecord;
    }
  }

  public static DynamodbStreamRecordUnmarshaller getInstance() {
    if (instance == null) {
      instance = new DynamodbStreamRecordUnmarshaller();
    }

    return instance;
  }
}

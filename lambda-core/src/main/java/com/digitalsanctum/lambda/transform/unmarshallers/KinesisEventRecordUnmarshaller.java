package com.digitalsanctum.lambda.transform.unmarshallers;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;
import com.fasterxml.jackson.core.JsonToken;

/**
 * @author Shane Witbeck
 * @since 3/31/17
 */
public class KinesisEventRecordUnmarshaller implements Unmarshaller<KinesisEvent.KinesisEventRecord, JsonUnmarshallerContext> {

  private static KinesisEventRecordUnmarshaller instance;

  private KinesisEventRecordUnmarshaller() {
  }
  

  public KinesisEvent.KinesisEventRecord unmarshall(JsonUnmarshallerContext context) throws Exception {
    KinesisEvent.KinesisEventRecord kinesisEventRecord = new KinesisEvent.KinesisEventRecord();
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
            kinesisEventRecord.setEventID((String) context.getUnmarshaller(String.class).unmarshall(context));
          }

          if (context.testExpression("eventName", targetDepth)) {
            context.nextToken();
            kinesisEventRecord.setEventName((String) context.getUnmarshaller(String.class).unmarshall(context));
          }

          if (context.testExpression("eventVersion", targetDepth)) {
            context.nextToken();
            kinesisEventRecord.setEventVersion((String) context.getUnmarshaller(String.class).unmarshall(context));
          }

          if (context.testExpression("eventSource", targetDepth)) {
            context.nextToken();
            kinesisEventRecord.setEventSource((String) context.getUnmarshaller(String.class).unmarshall(context));
          }

          if (context.testExpression("awsRegion", targetDepth)) {
            context.nextToken();
            kinesisEventRecord.setAwsRegion((String) context.getUnmarshaller(String.class).unmarshall(context));
          }

          if (context.testExpression("kinesis", targetDepth)) {
            context.nextToken();
            kinesisEventRecord.setKinesis(KinesisEventRecordJsonUnmarshaller.getInstance().unmarshall(context));
          }
          //eventSourceARN
          if (context.testExpression("eventSourceARN", targetDepth)) {
            context.nextToken();
            kinesisEventRecord.setEventSourceARN((String) context.getUnmarshaller(String.class).unmarshall(context));
          }
          if (context.testExpression("invokeIdentityARN", targetDepth)) {
            context.nextToken();
            kinesisEventRecord.setInvokeIdentityArn((String) context.getUnmarshaller(String.class).unmarshall(context));
          }
        }
      }

      return kinesisEventRecord;
    }
  }

  public static KinesisEventRecordUnmarshaller getInstance() {
    if (instance == null) {
      instance = new KinesisEventRecordUnmarshaller();
    }

    return instance;
  }
}

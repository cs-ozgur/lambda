package com.digitalsanctum.lambda.transform.unmarshallers;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;
import com.fasterxml.jackson.core.JsonToken;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * @author Shane Witbeck
 * @since 4/1/17
 */
public class KinesisEventRecordJsonUnmarshaller implements Unmarshaller<KinesisEvent.Record, JsonUnmarshallerContext> {
  private static KinesisEventRecordJsonUnmarshaller instance;

  public KinesisEventRecordJsonUnmarshaller() {
  }

  public KinesisEvent.Record unmarshall(JsonUnmarshallerContext context) throws Exception {
    KinesisEvent.Record record = new KinesisEvent.Record();
    int originalDepth = context.getCurrentDepth();
    String currentParentElement = context.getCurrentParentElement();
    int targetDepth = originalDepth + 1;
    JsonToken token = context.getCurrentToken();
    if(token == null) {
      token = context.nextToken();
    }

    if(token == JsonToken.VALUE_NULL) {
      return null;
    } else {
      for(; token != null; token = context.nextToken()) {
        if(token != JsonToken.FIELD_NAME && token != JsonToken.START_OBJECT) {
          if((token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT) && (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) && context.getCurrentDepth() <= originalDepth) {
            break;
          }
        } else {
          if(context.testExpression("SequenceNumber", targetDepth)) {
            context.nextToken();
            record.setSequenceNumber((String)context.getUnmarshaller(String.class).unmarshall(context));
          }

          if(context.testExpression("ApproximateArrivalTimestamp", targetDepth)) {
            context.nextToken();
            record.setApproximateArrivalTimestamp((Date)context.getUnmarshaller(Date.class).unmarshall(context));
          }

          if(context.testExpression("Data", targetDepth)) {
            context.nextToken();
            record.setData((ByteBuffer)context.getUnmarshaller(ByteBuffer.class).unmarshall(context));
          }

          if(context.testExpression("PartitionKey", targetDepth)) {
            context.nextToken();
            record.setPartitionKey((String)context.getUnmarshaller(String.class).unmarshall(context));
          }
          
          if (context.testExpression("KinesisSchemaVersion", targetDepth)) {
            context.nextToken();
            record.setKinesisSchemaVersion((String) context.getUnmarshaller(String.class).unmarshall(context));
          }
        }
      }

      return record;
    }
  }

  public static KinesisEventRecordJsonUnmarshaller getInstance() {
    if(instance == null) {
      instance = new KinesisEventRecordJsonUnmarshaller();
    }

    return instance;
  }
}

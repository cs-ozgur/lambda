package com.digitalsanctum.lambda.marshallers;

import com.amazonaws.SdkClientException;
import com.amazonaws.protocol.json.StructuredJsonGenerator;
import com.amazonaws.services.dynamodbv2.model.transform.StreamRecordJsonMarshaller;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

/**
 * @author Shane Witbeck
 * @since 3/11/17
 */
public class DynamodbStreamRecordJsonMarshaller {
  private static DynamodbStreamRecordJsonMarshaller instance;

  public static DynamodbStreamRecordJsonMarshaller getInstance() {
    if (instance == null)
      instance = new DynamodbStreamRecordJsonMarshaller();
    return instance;
  }

  /**
   * Marshall the given parameter object, and output to a SdkJsonGenerator
   */
  public void marshall(DynamodbEvent.DynamodbStreamRecord dynamodbStreamRecord, StructuredJsonGenerator jsonGenerator) {

    if (dynamodbStreamRecord == null) {
      throw new SdkClientException("Invalid argument passed to marshall(...)");
    }

    try {
      jsonGenerator.writeStartObject();

      if (dynamodbStreamRecord.getEventSourceARN() != null) {
        jsonGenerator.writeFieldName("eventSourceARN").writeValue(dynamodbStreamRecord.getEventSourceARN());
      }
      if (dynamodbStreamRecord.getEventID() != null) {
        jsonGenerator.writeFieldName("eventID").writeValue(dynamodbStreamRecord.getEventID());
      }
      if (dynamodbStreamRecord.getEventName() != null) {
        jsonGenerator.writeFieldName("eventName").writeValue(dynamodbStreamRecord.getEventName());
      }
      if (dynamodbStreamRecord.getEventVersion() != null) {
        jsonGenerator.writeFieldName("eventVersion").writeValue(dynamodbStreamRecord.getEventVersion());
      }
      if (dynamodbStreamRecord.getEventSource() != null) {
        jsonGenerator.writeFieldName("eventSource").writeValue(dynamodbStreamRecord.getEventSource());
      }
      if (dynamodbStreamRecord.getAwsRegion() != null) {
        jsonGenerator.writeFieldName("awsRegion").writeValue(dynamodbStreamRecord.getAwsRegion());
      }
      if (dynamodbStreamRecord.getDynamodb() != null) {
        jsonGenerator.writeFieldName("dynamodb");
        StreamRecordJsonMarshaller.getInstance().marshall(dynamodbStreamRecord.getDynamodb(), jsonGenerator);
      }

      jsonGenerator.writeEndObject();
    } catch (Throwable t) {
      throw new SdkClientException("Unable to marshall request to JSON: " + t.getMessage(), t);
    }
  }
}

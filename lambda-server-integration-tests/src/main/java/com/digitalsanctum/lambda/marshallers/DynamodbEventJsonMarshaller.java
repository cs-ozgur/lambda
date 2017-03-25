package com.digitalsanctum.lambda.marshallers;

import com.amazonaws.SdkClientException;
import com.amazonaws.protocol.json.StructuredJsonGenerator;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

/**
 * @author Shane Witbeck
 * @since 3/11/17
 */
public class DynamodbEventJsonMarshaller {
  private static DynamodbEventJsonMarshaller instance;

  public static DynamodbEventJsonMarshaller getInstance() {
    if (instance == null)
      instance = new DynamodbEventJsonMarshaller();
    return instance;
  }

  public void marshall(DynamodbEvent dynamodbEvent, StructuredJsonGenerator jsonGenerator) {

    if (dynamodbEvent == null) {
      throw new SdkClientException("Invalid argument passed to marshall(...)");
    }

    try {

      jsonGenerator.writeStartObject();
      if (dynamodbEvent.getRecords() != null) {
        DynamodbStreamRecordJsonMarshaller dynamodbStreamRecordJsonMarshaller = DynamodbStreamRecordJsonMarshaller.getInstance();
        jsonGenerator.writeFieldName("Records");
        jsonGenerator.writeStartArray();
        dynamodbEvent.getRecords().forEach(dynamodbStreamRecord -> dynamodbStreamRecordJsonMarshaller.marshall(dynamodbStreamRecord, jsonGenerator));
        jsonGenerator.writeEndArray();
      }
      jsonGenerator.writeEndObject();

    } catch (Throwable t) {
      throw new SdkClientException("Unable to marshall request to JSON: " + t.getMessage(), t);
    }
  }
}

package com.digitalsanctum.lambda.transform.marshallers;

import com.amazonaws.SdkClientException;
import com.amazonaws.protocol.MarshallLocation;
import com.amazonaws.protocol.MarshallingInfo;
import com.amazonaws.protocol.MarshallingType;
import com.amazonaws.protocol.ProtocolMarshaller;
import com.amazonaws.protocol.StructuredPojo;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

/**
 * @author Shane Witbeck
 * @since 3/27/17
 */
public class DynamodbStreamRecordJsonMarshaller {
  private static final MarshallingInfo<String> EVENTSOURCEARN_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("eventSourceArn").build();
  private static final MarshallingInfo<String> EVENTID_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("eventID").build();
  private static final MarshallingInfo<String> EVENTNAME_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("eventName").build();
  private static final MarshallingInfo<String> EVENTVERSION_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("eventVersion").build();
  private static final MarshallingInfo<String> EVENTSOURCE_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("eventSource").build();
  private static final MarshallingInfo<String> AWSREGION_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("awsRegion").build();
  private static final MarshallingInfo<StructuredPojo> DYNAMODB_BINDING = MarshallingInfo.builder(MarshallingType.STRUCTURED)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("dynamodb").build();
  private static final MarshallingInfo<StructuredPojo> USERIDENTITY_BINDING = MarshallingInfo.builder(MarshallingType.STRUCTURED)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("userIdentity").build();

  private static final DynamodbStreamRecordJsonMarshaller instance = new DynamodbStreamRecordJsonMarshaller();

  public static DynamodbStreamRecordJsonMarshaller getInstance() {
    return instance;
  }

  /**
   * Marshall the given parameter object.
   */
  public void marshall(DynamodbEvent.DynamodbStreamRecord dynamodbStreamRecord, ProtocolMarshaller protocolMarshaller) {

    if (dynamodbStreamRecord == null) {
      throw new SdkClientException("Invalid argument passed to marshall(...)");
    }

    try {
      protocolMarshaller.marshall(dynamodbStreamRecord.getEventSourceARN(), EVENTSOURCEARN_BINDING);
      protocolMarshaller.marshall(dynamodbStreamRecord.getEventID(), EVENTID_BINDING);
      protocolMarshaller.marshall(dynamodbStreamRecord.getEventName(), EVENTNAME_BINDING);
      protocolMarshaller.marshall(dynamodbStreamRecord.getEventVersion(), EVENTVERSION_BINDING);
      protocolMarshaller.marshall(dynamodbStreamRecord.getEventSource(), EVENTSOURCE_BINDING);
      protocolMarshaller.marshall(dynamodbStreamRecord.getAwsRegion(), AWSREGION_BINDING);
      protocolMarshaller.marshall(dynamodbStreamRecord.getDynamodb(), DYNAMODB_BINDING);
      protocolMarshaller.marshall(dynamodbStreamRecord.getUserIdentity(), USERIDENTITY_BINDING);
    } catch (Exception e) {
      throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
    }
  }
}

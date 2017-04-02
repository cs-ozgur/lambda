package com.digitalsanctum.lambda.transform.marshallers;

import com.amazonaws.SdkClientException;
import com.amazonaws.protocol.MarshallLocation;
import com.amazonaws.protocol.MarshallingInfo;
import com.amazonaws.protocol.MarshallingType;
import com.amazonaws.protocol.ProtocolMarshaller;
import com.amazonaws.protocol.StructuredPojo;

/**
 * @author Shane Witbeck
 * @since 3/31/17
 */
public class KinesisEventRecordExtJsonMarshaller {
  
  private static final MarshallingInfo<String> EVENTSOURCEARN_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("eventSourceARN").build();
  
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
  
  private static final MarshallingInfo<StructuredPojo> KINESIS_BINDING = MarshallingInfo.builder(MarshallingType.STRUCTURED)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("kinesis").build();
  
  private static final MarshallingInfo<String> USERIDENTITY_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("invokeIdentityArn").build();

  private static final KinesisEventRecordExtJsonMarshaller instance = new KinesisEventRecordExtJsonMarshaller();

  public static KinesisEventRecordExtJsonMarshaller getInstance() {
    return instance;
  }

  /**
   * Marshall the given parameter object.
   */
  public void marshall(KinesisEventRecordExt kinesisEventRecordExt, ProtocolMarshaller protocolMarshaller) {

    if (kinesisEventRecordExt == null) {
      throw new SdkClientException("Invalid argument passed to marshall(...)");
    }

    try {
      protocolMarshaller.marshall(kinesisEventRecordExt.getEventSourceARN(), EVENTSOURCEARN_BINDING);
      protocolMarshaller.marshall(kinesisEventRecordExt.getEventID(), EVENTID_BINDING);
      protocolMarshaller.marshall(kinesisEventRecordExt.getEventName(), EVENTNAME_BINDING);
      protocolMarshaller.marshall(kinesisEventRecordExt.getEventVersion(), EVENTVERSION_BINDING);
      protocolMarshaller.marshall(kinesisEventRecordExt.getEventSource(), EVENTSOURCE_BINDING);
      protocolMarshaller.marshall(kinesisEventRecordExt.getAwsRegion(), AWSREGION_BINDING);
      protocolMarshaller.marshall(kinesisEventRecordExt.getKinesis(), KINESIS_BINDING);
      protocolMarshaller.marshall(kinesisEventRecordExt.getInvokeIdentityArn(), USERIDENTITY_BINDING);
    } catch (Exception e) {
      throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
    }
  }
}

package com.digitalsanctum.lambda.transform.marshallers;

import com.amazonaws.SdkClientException;
import com.amazonaws.protocol.MarshallLocation;
import com.amazonaws.protocol.MarshallingInfo;
import com.amazonaws.protocol.MarshallingType;
import com.amazonaws.protocol.ProtocolMarshaller;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Shane Witbeck
 * @since 3/31/17
 */
public class KinesisEventJsonMarshaller {
  private static final MarshallingInfo<List> RECORDS_BINDING = MarshallingInfo.builder(MarshallingType.LIST)
      .marshallLocation(MarshallLocation.PAYLOAD)
      .marshallLocationName("Records")
      .build();

  private static final KinesisEventJsonMarshaller instance = new KinesisEventJsonMarshaller();

  public static KinesisEventJsonMarshaller getInstance() {
    return instance;
  }

  /**
   * Marshall the given parameter object.
   */
  public void marshall(KinesisEvent kinesisEvent, ProtocolMarshaller protocolMarshaller) {

    if (kinesisEvent == null) {
      throw new SdkClientException("Invalid argument passed to marshall(...)");
    }
    
    /*
      This is a hack to get around the fact that KinesisEvent.KinesisEventRecord doesn't implement StructuredPojo
      and fails during marshalling.
     */
    List<KinesisEventRecordExt> records = kinesisEvent.getRecords().stream()
        .map(KinesisEventRecordExt::new)
        .collect(Collectors.toList());

    try {
      protocolMarshaller.marshall(records, RECORDS_BINDING);
    } catch (Exception e) {
      throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
    }
  }
}

package com.digitalsanctum.lambda.transform.marshallers;

import com.amazonaws.SdkClientException;
import com.amazonaws.protocol.MarshallLocation;
import com.amazonaws.protocol.MarshallingInfo;
import com.amazonaws.protocol.MarshallingType;
import com.amazonaws.protocol.ProtocolMarshaller;

import java.nio.ByteBuffer;
import java.util.Date;

/**
 * @author Shane Witbeck
 * @since 4/2/17
 */
public class RecordExtJsonMarshaller {

  private static final MarshallingInfo<String> SEQUENCENUMBER_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("SequenceNumber").build();
  private static final MarshallingInfo<Date> APPROXIMATEARRIVALTIMESTAMP_BINDING = MarshallingInfo.builder(MarshallingType.DATE)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("ApproximateArrivalTimestamp").build();
  private static final MarshallingInfo<ByteBuffer> DATA_BINDING = MarshallingInfo.builder(MarshallingType.BYTE_BUFFER)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("Data").build();
  private static final MarshallingInfo<String> PARTITIONKEY_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("PartitionKey").build();
  private static final MarshallingInfo<String> KINESISSCHEMAVERSION_BINDING = MarshallingInfo.builder(MarshallingType.STRING)
      .marshallLocation(MarshallLocation.PAYLOAD).marshallLocationName("KinesisSchemaVersion").build();

  private static final RecordExtJsonMarshaller instance = new RecordExtJsonMarshaller();

  public static RecordExtJsonMarshaller getInstance() {
    return instance;
  }

  public void marshall(RecordExt record, ProtocolMarshaller protocolMarshaller) {
    if (record == null) {
      throw new SdkClientException("Invalid argument passed to marshall(...)");
    } else {
      try {
        protocolMarshaller.marshall(record.getSequenceNumber(), SEQUENCENUMBER_BINDING);
        protocolMarshaller.marshall(record.getApproximateArrivalTimestamp(), APPROXIMATEARRIVALTIMESTAMP_BINDING);
        protocolMarshaller.marshall(record.getData(), DATA_BINDING);
        protocolMarshaller.marshall(record.getPartitionKey(), PARTITIONKEY_BINDING);
        protocolMarshaller.marshall(record.getKinesisSchemaVersion(), KINESISSCHEMAVERSION_BINDING);
      } catch (Exception var4) {
        throw new SdkClientException("Unable to marshall request to JSON: " + var4.getMessage(), var4);
      }
    }
  }
}

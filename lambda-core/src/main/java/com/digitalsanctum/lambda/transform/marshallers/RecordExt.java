package com.digitalsanctum.lambda.transform.marshallers;

import com.amazonaws.annotation.SdkInternalApi;
import com.amazonaws.protocol.ProtocolMarshaller;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;

/**
 * @author Shane Witbeck
 * @since 4/2/17
 */
public class RecordExt extends com.amazonaws.services.kinesis.model.Record {
  
  private String kinesisSchemaVersion;
  
  public RecordExt(KinesisEvent.Record record) {
    setKinesisSchemaVersion(record.getKinesisSchemaVersion());
    setApproximateArrivalTimestamp(record.getApproximateArrivalTimestamp());
    setData(record.getData());
    setPartitionKey(record.getPartitionKey());
    setSequenceNumber(record.getSequenceNumber());
  }
  

  /**
   * Gets the schema version for the record
   *
   */
  public String getKinesisSchemaVersion() {
    return kinesisSchemaVersion;
  }

  /**
   * Sets the schema version for the record
   * @param kinesisSchemaVersion A string containing the schema version
   */
  public void setKinesisSchemaVersion(String kinesisSchemaVersion) {
    this.kinesisSchemaVersion = kinesisSchemaVersion;
  }

  /* (non-Javadoc)
   * @see com.amazonaws.services.kinesis.model.Record#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime
        * result
        + ((getKinesisSchemaVersion() == null) ? 0
        : getKinesisSchemaVersion().hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see com.amazonaws.services.kinesis.model.Record#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    RecordExt other = (RecordExt) obj;
    if (kinesisSchemaVersion == null) {
      if (other.kinesisSchemaVersion != null)
        return false;
    } else if (!kinesisSchemaVersion.equals(other.kinesisSchemaVersion))
      return false;
    return true;
  }

  @SdkInternalApi
  public void marshall(ProtocolMarshaller protocolMarshaller) {
    RecordExtJsonMarshaller.getInstance().marshall(this, protocolMarshaller);
  }
}

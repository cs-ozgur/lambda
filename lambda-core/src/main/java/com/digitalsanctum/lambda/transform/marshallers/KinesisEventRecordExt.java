package com.digitalsanctum.lambda.transform.marshallers;

import com.amazonaws.protocol.ProtocolMarshaller;
import com.amazonaws.protocol.StructuredPojo;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;

import java.io.Serializable;

/**
 * @author Shane Witbeck
 * @since 4/1/17
 */
public class KinesisEventRecordExt implements Serializable, Cloneable, StructuredPojo {
  
  public KinesisEventRecordExt(KinesisEvent.KinesisEventRecord kinesisEventRecord) {
    this.eventID = kinesisEventRecord.getEventID();
    this.eventName = kinesisEventRecord.getEventName();
    this.awsRegion = kinesisEventRecord.getAwsRegion();
    this.eventSourceARN = kinesisEventRecord.getEventSourceARN();
    this.eventVersion = kinesisEventRecord.getEventVersion();
    this.kinesis = new RecordExt(kinesisEventRecord.getKinesis());
    this.invokeIdentityArn = kinesisEventRecord.getInvokeIdentityArn();
    this.eventSource = kinesisEventRecord.getEventSource();
  }

  private String eventSource;

  private RecordExt kinesis;

  private String eventID;

  private String invokeIdentityArn;

  private String eventName;

  private String eventVersion;

  private String eventSourceARN;

  private String awsRegion;

  /**
   * Gets the source of the event
   */
  public String getEventSource() {
    return eventSource;
  }

  /**
   * Sets the source of the event
   *
   * @param eventSource A string representing the event source
   */
  public void setEventSource(String eventSource) {
    this.eventSource = eventSource;
  }

  /**
   * Gets the underlying Kinesis record associated with the event.
   */
  public RecordExt getKinesis() {
    return kinesis;
  }

  /**
   * Sets the underlying Kinesis record associated with the event.
   *
   * @param kinesis A Kineis record object.
   */
  public void setKinesis(RecordExt kinesis) {
    this.kinesis = kinesis;
  }

  /**
   * Gets the event id.
   */
  public String getEventID() {
    return eventID;
  }

  /**
   * Sets the event id
   *
   * @param eventID A string representing the event id.
   */
  public void setEventID(String eventID) {
    this.eventID = eventID;
  }

  /**
   * Gets then ARN for the identity used to invoke the Lambda Function.
   */
  public String getInvokeIdentityArn() {
    return invokeIdentityArn;
  }

  /**
   * Sets an ARN for the identity used to invoke the Lambda Function.
   *
   * @param invokeIdentityArn A string representing the invoke identity ARN
   */
  public void setInvokeIdentityArn(String invokeIdentityArn) {
    this.invokeIdentityArn = invokeIdentityArn;
  }

  /**
   * Gets the name of the event
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Sets the name of the event
   *
   * @param eventName A string containing the event name
   */
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  /**
   * Gets the event version
   */
  public String getEventVersion() {
    return eventVersion;
  }

  /**
   * Sets the event version
   *
   * @param eventVersion A string containing the event version
   */
  public void setEventVersion(String eventVersion) {
    this.eventVersion = eventVersion;
  }

  /**
   * Gets the ARN of the event source
   */
  public String getEventSourceARN() {
    return eventSourceARN;
  }

  /**
   * Sets the ARN of the event source
   *
   * @param eventSourceARN A string containing the event source ARN
   */
  public void setEventSourceARN(String eventSourceARN) {
    this.eventSourceARN = eventSourceARN;
  }

  /**
   * Gets the AWS region where the event originated
   */
  public String getAwsRegion() {
    return awsRegion;
  }

  /**
   * Sets the AWS region where the event originated
   *
   * @param awsRegion A string containing the AWS region
   */
  public void setAwsRegion(String awsRegion) {
    this.awsRegion = awsRegion;
  }

  @Override public void marshall(ProtocolMarshaller protocolMarshaller) {
    KinesisEventRecordExtJsonMarshaller.getInstance().marshall(this, protocolMarshaller);
  }
}

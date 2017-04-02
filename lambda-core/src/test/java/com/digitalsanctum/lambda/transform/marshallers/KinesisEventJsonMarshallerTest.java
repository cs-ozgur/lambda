package com.digitalsanctum.lambda.transform.marshallers;

import com.amazonaws.Request;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.protocol.OperationInfo;
import com.amazonaws.protocol.Protocol;
import com.amazonaws.protocol.ProtocolRequestMarshaller;
import com.amazonaws.protocol.json.JsonClientMetadata;
import com.amazonaws.protocol.json.JsonErrorShapeMetadata;
import com.amazonaws.protocol.json.SdkJsonProtocolFactory;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.digitalsanctum.lambda.transform.JsonUnmarshallerContextFactory;
import com.digitalsanctum.lambda.transform.unmarshallers.KinesisEventUnmarshaller;
import org.junit.Test;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 3/31/17
 */
public class KinesisEventJsonMarshallerTest {

  private static final JsonClientMetadata JSON_CLIENT_METADATA = new JsonClientMetadata()
      .withProtocolVersion("1.1")
      .withSupportsCbor(false) // override for Kinesalite
      .withSupportsIon(false)
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("InvalidArgumentException").withModeledClass(
              com.amazonaws.services.kinesis.model.InvalidArgumentException.class))
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("ResourceInUseException").withModeledClass(
              com.amazonaws.services.kinesis.model.ResourceInUseException.class))
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("ResourceNotFoundException").withModeledClass(
              com.amazonaws.services.kinesis.model.ResourceNotFoundException.class))
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("ExpiredIteratorException").withModeledClass(
              com.amazonaws.services.kinesis.model.ExpiredIteratorException.class))
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("ProvisionedThroughputExceededException").withModeledClass(
              com.amazonaws.services.kinesis.model.ProvisionedThroughputExceededException.class))
      .addErrorMetadata(
          new JsonErrorShapeMetadata().withErrorCode("LimitExceededException").withModeledClass(
              com.amazonaws.services.kinesis.model.LimitExceededException.class))
      .withBaseServiceExceptionClass(com.amazonaws.services.kinesis.model.AmazonKinesisException.class);


  private static final OperationInfo SDK_OPERATION_BINDING = OperationInfo.builder()
      .protocol(Protocol.AWS_JSON)
      .requestUri("/")
      .httpMethodName(HttpMethodName.POST)
      .hasExplicitPayloadMember(false)
      .hasPayloadMembers(true)
      .operationIdentifier("Kinesis_20131202.PutRecord")
      .serviceName("AmazonKinesis")
      .build();

  @Test
  public void testMarshalKinesisEvent() throws Exception {

    KinesisEvent.Record record = new KinesisEvent.Record();
    record.setKinesisSchemaVersion("1.0");
    record.setSequenceNumber("49545115243490985018280067714973144582180062593244200961");
    record.setApproximateArrivalTimestamp(new Date(1491156346532L));
    record.setPartitionKey("partitionKey-3");
    record.setData(ByteBuffer.wrap("foo".getBytes()));

    KinesisEvent.KinesisEventRecord kinesisEventRecord = new KinesisEvent.KinesisEventRecord();
    kinesisEventRecord.setEventSourceARN("arn:aws:kinesis:foo");
    kinesisEventRecord.setAwsRegion("us-west-2");
    kinesisEventRecord.setKinesis(record);
    kinesisEventRecord.setEventID("shardId-000000000000:49545115243490985018280067714973144582180062593244200961");
    kinesisEventRecord.setEventName("aws:kinesis:record");
    kinesisEventRecord.setEventSource("aws:kinesis");
    kinesisEventRecord.setEventVersion("1.0");

    List<KinesisEvent.KinesisEventRecord> records = new ArrayList<>();
    records.add(kinesisEventRecord);

    KinesisEvent originalKinesisEvent = new KinesisEvent();
    originalKinesisEvent.setRecords(records);

    // marshal

    SdkJsonProtocolFactory sdkJsonProtocolFactory = new SdkJsonProtocolFactory(JSON_CLIENT_METADATA);
    ProtocolRequestMarshaller protocolRequestMarshaller = sdkJsonProtocolFactory
        .createProtocolMarshaller(SDK_OPERATION_BINDING, null);

    protocolRequestMarshaller.startMarshalling();

    KinesisEventJsonMarshaller.getInstance().marshall(originalKinesisEvent, protocolRequestMarshaller);

    Request request = protocolRequestMarshaller.finishMarshalling();

    // unmarshal

    InputStream inputJson = request.getContent();
    JsonUnmarshallerContext jsonUnmarshallerContext = new JsonUnmarshallerContextFactory()
        .getJsonUnmarshallerContext(inputJson);
    
    Object unmarshalledObject = KinesisEventUnmarshaller.getInstance().unmarshall(jsonUnmarshallerContext);

    // assertions

    assertThat(unmarshalledObject.getClass().getName(), is(KinesisEvent.class.getName()));

    KinesisEvent unmarshalledEvent = (KinesisEvent) unmarshalledObject;
    assertThat(unmarshalledEvent.getRecords().size(), is(originalKinesisEvent.getRecords().size()));
    assertThat(unmarshalledEvent.getRecords().get(0).getKinesis().getClass().getName(),
        is(originalKinesisEvent.getRecords().get(0).getKinesis().getClass().getName()));
  }
}


/*
Sample test event from AWS console:

{
  "Records": [
    {
      "eventID": "shardId-000000000000:49545115243490985018280067714973144582180062593244200961",
      "eventVersion": "1.0",
      "kinesis": {
        "approximateArrivalTimestamp": 1428537600,
        "partitionKey": "partitionKey-3",
        "data": "SGVsbG8sIHRoaXMgaXMgYSB0ZXN0IDEyMy4=",
        "kinesisSchemaVersion": "1.0",
        "sequenceNumber": "49545115243490985018280067714973144582180062593244200961"
      },
      "invokeIdentityArn": "arn:aws:iam::EXAMPLE",
      "eventName": "aws:kinesis:record",
      "eventSourceARN": "arn:aws:kinesis:EXAMPLE",
      "eventSource": "aws:kinesis",
      "awsRegion": "us-east-1"
    }
  ]
}

 */
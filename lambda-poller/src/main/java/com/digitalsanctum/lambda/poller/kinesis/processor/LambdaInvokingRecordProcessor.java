package com.digitalsanctum.lambda.poller.kinesis.processor;

import com.amazonaws.Request;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.protocol.OperationInfo;
import com.amazonaws.protocol.Protocol;
import com.amazonaws.protocol.ProtocolRequestMarshaller;
import com.amazonaws.protocol.json.JsonClientMetadata;
import com.amazonaws.protocol.json.JsonErrorShapeMetadata;
import com.amazonaws.protocol.json.SdkJsonProtocolFactory;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.LogType;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.util.IOUtils;
import com.digitalsanctum.lambda.transform.marshallers.KinesisEventJsonMarshaller;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author Shane Witbeck
 * @since 3/31/17
 */
public class LambdaInvokingRecordProcessor implements IRecordProcessor {

  private static final Logger log = LoggerFactory.getLogger(LambdaInvokingRecordProcessor.class);

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

  private String kinesisShardId;

  // Check pointing interval
  private static final long CHECKPOINT_INTERVAL_MILLIS = 1000L;
  private long nextCheckpointTimeInMillis;

  private AWSLambda awsLambda;
  private String functionName;

  public LambdaInvokingRecordProcessor(final AwsClientBuilder.EndpointConfiguration lambdaEndpointConfiguration,
                                       final String functionName) {
    this.awsLambda = AWSLambdaClientBuilder.standard().withEndpointConfiguration(lambdaEndpointConfiguration).build();
    this.functionName = functionName;
  }

  @Override
  public void initialize(InitializationInput initializationInput) {
    log.info("Initializing record processor for shard: " + initializationInput.getShardId());
    this.kinesisShardId = initializationInput.getShardId();
    nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
  }

  @Override
  public void processRecords(ProcessRecordsInput processRecordsInput) {
    processRecordsInput.getRecords().forEach(this::processRecord);

    // Checkpoint once every checkpoint interval
    if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
      checkpoint(processRecordsInput.getCheckpointer());
      nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
    }
  }

  private void processRecord(Record record) {

    ByteBuffer bb = record.getData();
    String data = new String(bb.array(), Charset.forName("UTF-8"));
    log.info("<<< received: {}", data);
    
    KinesisEvent.KinesisEventRecord kinesisEventRecord = new KinesisEvent.KinesisEventRecord();
    kinesisEventRecord.setEventID("id"); // TODO determine more accurate value
    kinesisEventRecord.setEventName("aws:kinesis:record");

    KinesisEvent.Record kinesis = new KinesisEvent.Record();
    kinesis.setPartitionKey(record.getPartitionKey());
    kinesis.setSequenceNumber(record.getSequenceNumber());
    kinesis.setData(bb);
    kinesis.setKinesisSchemaVersion("1.0");
    kinesisEventRecord.setKinesis(kinesis);
    
    // TODO handle batch size. for now, send one record at a time
    KinesisEvent kinesisEvent = new KinesisEvent();
    kinesisEvent.setRecords(ImmutableList.of(kinesisEventRecord));

    // marshal
    SdkJsonProtocolFactory sdkJsonProtocolFactory = new SdkJsonProtocolFactory(JSON_CLIENT_METADATA);
    ProtocolRequestMarshaller protocolRequestMarshaller = sdkJsonProtocolFactory
        .createProtocolMarshaller(SDK_OPERATION_BINDING, null);
    protocolRequestMarshaller.startMarshalling();
    KinesisEventJsonMarshaller.getInstance().marshall(kinesisEvent, protocolRequestMarshaller);
    Request request = protocolRequestMarshaller.finishMarshalling();

    // convert InputStream to ByteBuffer 
    InputStream contentInputStream = request.getContent();
    byte[] contentByteArr = new byte[0];
    try {
      contentByteArr = IOUtils.toByteArray(contentInputStream);
    } catch (IOException e) {
      e.printStackTrace();
    }
    log.info("payload: {}", new String(contentByteArr));
    
    ByteBuffer payload = ByteBuffer.wrap(contentByteArr);

    // prepare Lambda invoke request with KinesisEvent as payload
    InvokeRequest invokeRequest = new InvokeRequest();
    invokeRequest.setInvocationType(InvocationType.RequestResponse);
    invokeRequest.setPayload(payload);
    invokeRequest.setLogType(LogType.Tail);
    invokeRequest.setFunctionName(functionName);

    log.info("invoking {}", invokeRequest.toString());
    InvokeResult result = awsLambda.invoke(invokeRequest);
    if (result.getFunctionError() != null) {
      log.error(result.toString());
    } else {
      log.info(result.toString());
    }
    // TODO handle retries
    
  }

  @Override
  public void shutdown(ShutdownInput shutdownInput) {
    log.info("Shutting down record processor for shard: " + kinesisShardId);
    // Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
    if (shutdownInput.getShutdownReason() == ShutdownReason.TERMINATE) {
      checkpoint(shutdownInput.getCheckpointer());
    }
  }

  private void checkpoint(IRecordProcessorCheckpointer checkpointer) {
    log.info("Checkpointing shard " + kinesisShardId);
    try {
      checkpointer.checkpoint();
    } catch (ShutdownException se) {
      // Ignore checkpoint if the processor instance has been shutdown (fail over).
      log.info("Caught shutdown exception, skipping checkpoint.", se);
    } catch (ThrottlingException e) {
      // Skip checkpoint when throttled. In practice, consider a backoff and retry policy.
      log.error("Caught throttling exception, skipping checkpoint.", e);
    } catch (InvalidStateException e) {
      // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
      log.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
    }
  }
}

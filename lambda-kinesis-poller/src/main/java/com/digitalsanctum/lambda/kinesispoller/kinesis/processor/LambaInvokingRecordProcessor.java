package com.digitalsanctum.lambda.kinesispoller.kinesis.processor;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.protocol.json.SdkJsonGenerator;
import com.amazonaws.protocol.json.StructuredJsonGenerator;
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
import com.fasterxml.jackson.core.JsonFactory;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author Shane Witbeck
 * @since 3/31/17
 */
public class LambaInvokingRecordProcessor implements IRecordProcessor {

  private static final Logger log = LoggerFactory.getLogger(SimpleRecordProcessor.class);

  private String kinesisShardId;

  // Check pointing interval
  private static final long CHECKPOINT_INTERVAL_MILLIS = 1000L;
  private long nextCheckpointTimeInMillis;

  private AWSLambda awsLambda;
  private String functionName;

  public LambaInvokingRecordProcessor(final AwsClientBuilder.EndpointConfiguration lambdaEndpointConfiguration,
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

    KinesisEvent kinesisEvent = new KinesisEvent();
    
    KinesisEvent.KinesisEventRecord kinesisEventRecord = new KinesisEvent.KinesisEventRecord();
    kinesisEventRecord.setEventID("id");
    kinesisEventRecord.setEventName("eventname");

    KinesisEvent.Record kinesis = new KinesisEvent.Record();
    kinesis.setPartitionKey(String.valueOf(System.currentTimeMillis()));
    kinesis.setSequenceNumber(String.valueOf(System.currentTimeMillis()));
    kinesis.setData(bb);
    kinesisEventRecord.setKinesis(kinesis);
    
    // TODO handle batch size. for now, send one record at a time

    kinesisEvent.setRecords(ImmutableList.of(kinesisEventRecord));


//    return new String(mapper.writeValueAsBytes(event));

    // TODO marshal KinesisEvent and set as payload

    /*JsonFactory jsonFactory = new JsonFactory();
    StructuredJsonGenerator structuredJsonGenerator = new SdkJsonGenerator(jsonFactory, "application/json");

    DynamodbEventJsonMarshaller.getInstance().marshall(event, structuredJsonGenerator);

    return new String(structuredJsonGenerator.getBytes());
    

    
    InvokeRequest invokeRequest = new InvokeRequest();
    invokeRequest.setInvocationType(InvocationType.Event);
    invokeRequest.setPayload(testRequestJson);
    invokeRequest.setLogType(LogType.Tail);
    invokeRequest.setFunctionName(functionName);


    InvokeResult result = awsLambda.invoke(invokeRequest);
    System.out.println(result);*/
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

package com.digitalsanctum.lambda.poller.kinesis.processor;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author Shane Witbeck
 * @since 4/3/16
 */
public class SimpleRecordProcessor implements IRecordProcessor {

  private static final Logger log = LoggerFactory.getLogger(SimpleRecordProcessor.class);

  private String kinesisShardId;

  // Check pointing interval
  private static final long CHECKPOINT_INTERVAL_MILLIS = 1000L;
  private long nextCheckpointTimeInMillis;

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

  @Override
  public void shutdown(ShutdownInput shutdownInput) {
    log.info("Shutting down record processor for shard: " + kinesisShardId);
    // Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
    if (shutdownInput.getShutdownReason() == ShutdownReason.TERMINATE) {
      checkpoint(shutdownInput.getCheckpointer());
    }
  }

  private void processRecord(Record record) {
    ByteBuffer bb = record.getData();
    String data = new String(bb.array(), Charset.forName("UTF-8"));
    log.info("<<< received: {}", data);
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


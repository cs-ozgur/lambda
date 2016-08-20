package com.digitalsanctum.lambda.kinesispoller.kinesis.processor;

import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 4/3/16
 */
public class RecordProcessor implements IRecordProcessor {

  private static final Logger log = LoggerFactory.getLogger(RecordProcessor.class);
  private String kinesisShardId;

  // Checkpointing interval
  private static final long CHECKPOINT_INTERVAL_MILLIS = 1000L;
  private long nextCheckpointTimeInMillis;

  /**
   * {@inheritDoc}
   */
  @Override
  public void initialize(String shardId) {
    log.info("Initializing record processor for shard: " + shardId);
    this.kinesisShardId = shardId;
    nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void processRecords(List<Record> records, IRecordProcessorCheckpointer checkpointer) {

    records.forEach(this::processRecord);

    // Checkpoint once every checkpoint interval
    if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
      checkpoint(checkpointer);
      nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
    }
  }

  private void processRecord(Record record) {

    ByteBuffer bb = record.getData();
    String data = new String(bb.array(), Charset.forName("UTF-8"));
    log.info("<<< received: {}", data);
    
    // TODO send to subscribed event sinks        

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {
    log.info("Shutting down record processor for shard: " + kinesisShardId);
    // Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
    if (reason == ShutdownReason.TERMINATE) {
      checkpoint(checkpointer);
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

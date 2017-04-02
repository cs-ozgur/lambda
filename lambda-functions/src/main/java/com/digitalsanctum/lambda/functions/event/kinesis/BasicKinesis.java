package com.digitalsanctum.lambda.functions.event.kinesis;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 8/20/16
 */
public class BasicKinesis {

  private static final Logger log = LoggerFactory.getLogger(BasicKinesis.class);
  
  public void handler(KinesisEvent kinesisEvent) {
    log.info("received KinesisEvent");

    if (kinesisEvent.getRecords().isEmpty()) {
      log.info("no records!");
      return;
    }
    
    List<KinesisEvent.KinesisEventRecord> kinesisEventRecord = kinesisEvent.getRecords();
    for (KinesisEvent.KinesisEventRecord eventRecord : kinesisEventRecord) {
      KinesisEvent.Record record = eventRecord.getKinesis();
      log.info(record.toString());

      ByteBuffer bb = record.getData();
      String data = new String(bb.array(), Charset.forName("UTF-8"));
      log.info("data: {}", data);
    }
  }
}

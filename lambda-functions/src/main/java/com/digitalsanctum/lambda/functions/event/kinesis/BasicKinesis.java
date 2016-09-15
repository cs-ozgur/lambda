package com.digitalsanctum.lambda.functions.event.kinesis;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 8/20/16
 */
public class BasicKinesis {
  
  public void handler(KinesisEvent kinesisEvent, Context context) {
    LambdaLogger logger = context.getLogger();
    
    List<KinesisEvent.KinesisEventRecord> kinesisEventRecord = kinesisEvent.getRecords();

    for (KinesisEvent.KinesisEventRecord eventRecord : kinesisEventRecord) {
      KinesisEvent.Record record = eventRecord.getKinesis();
      ByteBuffer bb = record.getData();
      String data = new String(bb.array(), Charset.forName("UTF-8"));
      logger.log("received: " + data);
    }
  }
}

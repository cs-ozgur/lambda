package com.digitalsanctum.lambda.functions.event.dynamodb;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Shane Witbeck
 * @since 9/10/16
 */
public class BasicDynamoDb {

  private static final Logger log = LoggerFactory.getLogger(BasicDynamoDb.class);

  public void handler(DynamodbEvent dynamodbEvent) {    
    List<DynamodbEvent.DynamodbStreamRecord> dynamodbEventRecords = dynamodbEvent.getRecords();
    for (DynamodbEvent.DynamodbStreamRecord eventRecord : dynamodbEventRecords) {
      StreamRecord record = eventRecord.getDynamodb();
      Map<String, AttributeValue> map = record.getNewImage();
      log.info("received {}", map.toString());
    }
  }
}

package com.digitalsanctum.lambda.functions.event.dynamodb;

import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Shane Witbeck
 * @since 9/10/16
 */
public class BasicDynamoDb {

  private static final Logger log = LoggerFactory.getLogger(BasicDynamoDb.class);

  public void handler(DynamodbEvent dynamodbEvent) {    
    log.info("received DynamoDB event");
    
    if (dynamodbEvent.getRecords().isEmpty()) {
      log.info("no records!");
    }
    
    List<DynamodbEvent.DynamodbStreamRecord> dynamodbEventRecords = dynamodbEvent.getRecords();
    for (DynamodbEvent.DynamodbStreamRecord eventRecord : dynamodbEventRecords) {
      StreamRecord record = eventRecord.getDynamodb();
      log.info(record.toString());
    }
  }
}

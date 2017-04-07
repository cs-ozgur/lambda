package com.digitalsanctum.lambda.server.util;

import java.time.Instant;

/**
 * @author Shane Witbeck
 * @since 7/23/16
 */
public class ArnUtils {
  
  // arn:aws:lambda:local:111111111111:function:functionName
  public static String functionArn(String functionName) {                                      
    return eventSourceArn("lambda", "function:", functionName);
  }
  
  // arn:aws:dynamodb:us-east-1:111111111111:table/tableName/stream/2017-02-12T01:53:13.713
  public static String dynamodbStreamEventSourceArn(String tableName) {
    String timestamp = Instant.now().toString();
    String name = tableName + "/stream/" + timestamp;    
    return eventSourceArn("dynamodb", "table/", name);
  }

  // arn:aws:kinesis:us-east-1:111111111111:stream/monkey-slack
  public static String kinesisStreamArn(String streamName) {
    return eventSourceArn("kinesis", "stream/", streamName);
  }
  
  public static String eventSourceArn(String sourceType, String prefix, String name) {
    return "arn:aws:" + sourceType + ":local:111111111111:" + prefix + name;
  }
}

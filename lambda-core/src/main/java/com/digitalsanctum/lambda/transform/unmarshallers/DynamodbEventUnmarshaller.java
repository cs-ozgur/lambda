package com.digitalsanctum.lambda.transform.unmarshallers;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.Unmarshaller;
import com.fasterxml.jackson.core.JsonToken;

import java.util.ArrayList;
import java.util.List;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NULL;

/**
 * @author Shane Witbeck
 * @since 3/11/17
 */
public class DynamodbEventUnmarshaller implements Unmarshaller<DynamodbEvent, JsonUnmarshallerContext> {

  public DynamodbEvent unmarshall(JsonUnmarshallerContext context) throws Exception {
    DynamodbEvent dynamodbEvent = new DynamodbEvent();

    int originalDepth = context.getCurrentDepth();
    String currentParentElement = context.getCurrentParentElement();
    int targetDepth = originalDepth + 1;

    JsonToken token = context.getCurrentToken();
    if (token == null)
      token = context.nextToken();
    if (token == VALUE_NULL) {
      return null;
    }

    List<DynamodbEvent.DynamodbStreamRecord> records = new ArrayList<>();
    
    while (true) {
      if (token == null)
        break;

      if (token == FIELD_NAME || token == START_OBJECT) {
        if (context.testExpression("Records", targetDepth)) {
          context.nextToken();
          // RecordJsonUnmarshaller
          DynamodbEvent.DynamodbStreamRecord record = DynamodbStreamRecordUnmarshaller.getInstance().unmarshall(context);
          records.add(record);
          
        }
        
      } else if (token == END_ARRAY || token == END_OBJECT) {
        if (context.getLastParsedParentElement() == null || context.getLastParsedParentElement().equals(currentParentElement)) {
          if (context.getCurrentDepth() <= originalDepth)
            break;
        }
      }
      token = context.nextToken();
    }
    
    dynamodbEvent.setRecords(records);

    return dynamodbEvent;
  }

  private static DynamodbEventUnmarshaller instance;

  public static DynamodbEventUnmarshaller getInstance() {
    if (instance == null)
      instance = new DynamodbEventUnmarshaller();
    return instance;
  }
}

package com.digitalsanctum.lambda.transform.unmarshallers;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
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
 * @since 3/31/17
 */
public class KinesisEventUnmarshaller implements Unmarshaller<KinesisEvent, JsonUnmarshallerContext> {

  public KinesisEvent unmarshall(JsonUnmarshallerContext context) throws Exception {
    KinesisEvent kinesisEvent = new KinesisEvent();

    int originalDepth = context.getCurrentDepth();
    String currentParentElement = context.getCurrentParentElement();
    int targetDepth = originalDepth + 1;

    JsonToken token = context.getCurrentToken();
    if (token == null)
      token = context.nextToken();
    if (token == VALUE_NULL) {
      return null;
    }

    List<KinesisEvent.KinesisEventRecord> records = new ArrayList<>();

    while (true) {
      if (token == null)
        break;

      if (token == FIELD_NAME || token == START_OBJECT) {
        if (context.testExpression("Records", targetDepth)) {
          context.nextToken();

          KinesisEvent.KinesisEventRecord record = KinesisEventRecordUnmarshaller.getInstance().unmarshall(context);
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

    kinesisEvent.setRecords(records);

    return kinesisEvent;
  }

  private static KinesisEventUnmarshaller instance;

  public static KinesisEventUnmarshaller getInstance() {
    if (instance == null)
      instance = new KinesisEventUnmarshaller();
    return instance;
  }
}

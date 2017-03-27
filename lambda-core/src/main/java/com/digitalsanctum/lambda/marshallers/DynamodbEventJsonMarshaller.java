package com.digitalsanctum.lambda.marshallers;

import com.amazonaws.SdkClientException;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.protocol.MarshallLocation;
import com.amazonaws.protocol.MarshallingInfo;
import com.amazonaws.protocol.MarshallingType;
import com.amazonaws.protocol.OperationInfo;
import com.amazonaws.protocol.Protocol;
import com.amazonaws.protocol.ProtocolMarshaller;
import com.amazonaws.protocol.ProtocolRequestMarshaller;
import com.amazonaws.protocol.json.StructuredJsonGenerator;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import java.util.List;

/**
 * @author Shane Witbeck
 * @since 3/27/17
 */
public class DynamodbEventJsonMarshaller {

  private static final MarshallingInfo<List> RECORDS_BINDING = MarshallingInfo.builder(MarshallingType.LIST)
      .marshallLocation(MarshallLocation.PAYLOAD)
      .marshallLocationName("Records")
      .build();

  private static final DynamodbEventJsonMarshaller instance = new DynamodbEventJsonMarshaller();

  public static DynamodbEventJsonMarshaller getInstance() {
    return instance;
  }

  /**
   * Marshall the given parameter object.
   */
  public void marshall(DynamodbEvent dynamodbEvent, ProtocolMarshaller protocolMarshaller) {

    if (dynamodbEvent == null) {
      throw new SdkClientException("Invalid argument passed to marshall(...)");
    }

    try {
      protocolMarshaller.marshall(dynamodbEvent.getRecords(), RECORDS_BINDING);
    } catch (Exception e) {
      throw new SdkClientException("Unable to marshall request to JSON: " + e.getMessage(), e);
    }
  }
}

package com.digitalsanctum.lambda.service.serialization;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

import static com.amazonaws.util.DateUtils.formatServiceSpecificDate;

/**
 * @author Shane Witbeck
 * @since 3/11/17
 */
public class CreateEventSourceMappingResultSerializer extends StdSerializer<CreateEventSourceMappingResult> {

  public CreateEventSourceMappingResultSerializer() {
    super(CreateEventSourceMappingResult.class);
  }

  @Override
  public void serialize(CreateEventSourceMappingResult value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    gen.writeStringField("UUID", value.getUUID());
    gen.writeStringField("StateTransitionReason", value.getStateTransitionReason());
    gen.writeStringField("LastModified", formatServiceSpecificDate(value.getLastModified()));
    gen.writeNumberField("BatchSize", value.getBatchSize());
    gen.writeStringField("State", value.getState());
    gen.writeStringField("FunctionArn", value.getFunctionArn());
    gen.writeStringField("EventSourceArn", value.getEventSourceArn());
    gen.writeStringField("LastProcessingResult", value.getLastProcessingResult());
    gen.writeEndObject();
  }
}

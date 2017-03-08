package com.digitalsanctum.lambda.service.serialization;

import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

import static com.amazonaws.util.DateUtils.formatServiceSpecificDate;

/**
 * @author Shane Witbeck
 * @since 3/6/17
 */
public class EventSourceMappingConfigurationSerializer extends StdSerializer<EventSourceMappingConfiguration> {
  
  public EventSourceMappingConfigurationSerializer() {
    super(EventSourceMappingConfiguration.class);
  }

  @Override
  public void serialize(EventSourceMappingConfiguration value, JsonGenerator gen, SerializerProvider provider) throws IOException {
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

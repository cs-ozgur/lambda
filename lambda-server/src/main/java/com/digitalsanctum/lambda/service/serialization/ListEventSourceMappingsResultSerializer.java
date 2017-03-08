package com.digitalsanctum.lambda.service.serialization;

import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * @author Shane Witbeck
 * @since 3/6/17
 */
public class ListEventSourceMappingsResultSerializer extends StdSerializer<ListEventSourceMappingsResult> {

  public ListEventSourceMappingsResultSerializer() {
    super(ListEventSourceMappingsResult.class);
  }

  @Override
  public void serialize(ListEventSourceMappingsResult value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    gen.writeArrayFieldStart("EventSourceMappings");
    value.getEventSourceMappings().forEach(eventSourceMappingConfiguration -> {
      try {
        new EventSourceMappingConfigurationSerializer().serialize(eventSourceMappingConfiguration, gen, provider);        
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    gen.writeEndArray();
  }
}

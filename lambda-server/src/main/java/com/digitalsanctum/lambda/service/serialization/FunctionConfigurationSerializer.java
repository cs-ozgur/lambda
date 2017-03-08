package com.digitalsanctum.lambda.service.serialization;

import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * @author Shane Witbeck
 * @since 3/7/17
 */
public class FunctionConfigurationSerializer extends StdSerializer<FunctionConfiguration> {
  
  public FunctionConfigurationSerializer() {
    super(FunctionConfiguration.class);
  }

  @Override
  public void serialize(FunctionConfiguration value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();
    gen.writeStringField("FunctionName", value.getFunctionName());
    gen.writeEndObject();
  }
}

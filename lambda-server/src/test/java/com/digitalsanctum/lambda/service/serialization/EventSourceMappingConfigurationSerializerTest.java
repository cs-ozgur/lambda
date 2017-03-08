package com.digitalsanctum.lambda.service.serialization;

import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

/**
 * @author Shane Witbeck
 * @since 3/7/17
 */
public class EventSourceMappingConfigurationSerializerTest {
  @Test
  public void testSerialize() throws Exception {

    // create custom ObjectMapper for AWS SDK
    final ObjectMapper awsSdkObjectMapper = new ObjectMapper();
    awsSdkObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UpperCamelCaseStrategy.UPPER_CAMEL_CASE);

    // custom deserializers
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(EventSourceMappingConfiguration.class, new EventSourceMappingConfigurationSerializer());
    simpleModule.addSerializer(ListEventSourceMappingsResult.class, new ListEventSourceMappingsResultSerializer());
    awsSdkObjectMapper.registerModule(simpleModule);

    EventSourceMappingConfiguration eventSourceMappingConfiguration = new EventSourceMappingConfiguration()
        .withUUID(UUID.randomUUID().toString())
        .withStateTransitionReason("User action")
        .withLastModified(new Date())
        .withBatchSize(100)
        .withState("Enabled")
        .withFunctionArn("arn:aws:lambda:us-east-1:233367263614:function:launch-entries-metrics-test:test")
        .withEventSourceArn("arn:aws:dynamodb:us-east-1:233367263614:table/launch.launchwaitline.launch-entry/stream/2016-09-09T18:41:44.506")
        .withLastProcessingResult("PROBLEM: internal Lambda error. Please contact Lambda customer support.");

    String out = awsSdkObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventSourceMappingConfiguration);
    System.out.println(out);

  }
}

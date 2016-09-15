package com.digitalsanctum.lambda.server.service;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsRequest;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import jersey.repackaged.com.google.common.collect.ImmutableList;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Shane Witbeck
 * @since 8/28/16
 */
public class InMemoryEventSourceMappingService implements EventSourceMappingService {

  // key'd on uUID
  private static final Map<String, EventSourceMappingConfiguration> MAPPINGS = new HashMap<>();
  

  @Override
  public ListEventSourceMappingsResult listEventSourceMappingConfigurations(ListEventSourceMappingsRequest listEventSourceMappingsRequest) {


    // TODO filtering
    
    ListEventSourceMappingsResult result = new ListEventSourceMappingsResult();
    result.setEventSourceMappings(ImmutableList.copyOf(MAPPINGS.values()));
    
    return result;
  }

  @Override
  public EventSourceMappingConfiguration getEventSourceMappingConfiguration(String uUID) {
    return MAPPINGS.get(uUID);
  }

  @Override
  public EventSourceMappingConfiguration deleteEventSourceMappingConfiguration(String uUID) {
    return MAPPINGS.remove(uUID);
  }

  @Override
  public CreateEventSourceMappingResult createEventSourceMapping(CreateEventSourceMappingRequest createEventSourceMappingRequest) {
    
    // TODO function name -> function ARN
    
    EventSourceMappingConfiguration eventSourceMappingConfiguration = new EventSourceMappingConfiguration()
        .withUUID(UUID.randomUUID().toString())
        .withBatchSize(createEventSourceMappingRequest.getBatchSize())
        .withEventSourceArn(createEventSourceMappingRequest.getEventSourceArn())
        .withLastModified(new Date())
        .withState("Enabled");
    
    // TODO more props
    
    MAPPINGS.put(eventSourceMappingConfiguration.getUUID(), eventSourceMappingConfiguration);

    return new CreateEventSourceMappingResult()
        .withUUID(eventSourceMappingConfiguration.getUUID())
        .withBatchSize(eventSourceMappingConfiguration.getBatchSize())
        .withEventSourceArn(eventSourceMappingConfiguration.getEventSourceArn())
        .withLastModified(eventSourceMappingConfiguration.getLastModified())
        .withState("Enabled");
  }

  @Override
  public EventSourceMappingConfiguration updateEventSourceMappingConfiguration(String uUID, int batchSize, boolean enabled, String functionName) {
    
    EventSourceMappingConfiguration mapping = MAPPINGS.get(uUID);
    mapping.setBatchSize(batchSize);

    // TODO
    
    return mapping;
  }
}

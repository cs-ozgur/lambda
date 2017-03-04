package com.digitalsanctum.lambda.server.service.inmemory;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsRequest;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import com.digitalsanctum.lambda.server.service.EventSourceMappingService;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.digitalsanctum.lambda.server.util.ArnUtils.functionArn;
import static jersey.repackaged.com.google.common.collect.ImmutableList.copyOf;

/**
 * @author Shane Witbeck
 * @since 8/28/16
 */
public class InMemoryEventSourceMappingService implements EventSourceMappingService {

  // key'd on uUID
  private static final Map<String, EventSourceMappingConfiguration> MAPPINGS = new ConcurrentHashMap<>();
  

  @Override
  public ListEventSourceMappingsResult listEventSourceMappingConfigurations(ListEventSourceMappingsRequest listEventSourceMappingsRequest) {

    // TODO filtering
    
    ListEventSourceMappingsResult result = new ListEventSourceMappingsResult();
    result.setEventSourceMappings(copyOf(MAPPINGS.values()));
    
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
    
    String functionArn = functionArn(createEventSourceMappingRequest.getFunctionName());
    
    EventSourceMappingConfiguration eventSourceMappingConfiguration = new EventSourceMappingConfiguration()
        .withUUID(UUID.randomUUID().toString())
        .withBatchSize(createEventSourceMappingRequest.getBatchSize())
        .withEventSourceArn(createEventSourceMappingRequest.getEventSourceArn())
        .withFunctionArn(functionArn)
        .withLastModified(new Date())
        .withState("Enabled");
    
    MAPPINGS.put(eventSourceMappingConfiguration.getUUID(), eventSourceMappingConfiguration);

    return new CreateEventSourceMappingResult()
        .withUUID(eventSourceMappingConfiguration.getUUID())
        .withBatchSize(eventSourceMappingConfiguration.getBatchSize())
        .withEventSourceArn(eventSourceMappingConfiguration.getEventSourceArn())
        .withLastModified(eventSourceMappingConfiguration.getLastModified())
        .withState("Enabled");
  }

  @Override
  public EventSourceMappingConfiguration updateEventSourceMappingConfiguration(String uUID, 
                                                                               int batchSize, 
                                                                               String state) {    
    EventSourceMappingConfiguration mapping = MAPPINGS.get(uUID);
    mapping.setBatchSize(batchSize);
    mapping.setState(state);

    MAPPINGS.put(uUID, mapping);
    return mapping;
  }
}

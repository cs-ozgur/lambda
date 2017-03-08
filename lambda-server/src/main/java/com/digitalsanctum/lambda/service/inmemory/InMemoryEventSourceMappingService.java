package com.digitalsanctum.lambda.service.inmemory;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.DeleteEventSourceMappingResult;
import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.amazonaws.services.lambda.model.GetEventSourceMappingResult;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsRequest;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import com.amazonaws.services.lambda.model.UpdateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.UpdateEventSourceMappingResult;
import com.digitalsanctum.lambda.service.EventSourceMappingService;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.digitalsanctum.lambda.server.util.ArnUtils.functionArn;
import static java.lang.Boolean.TRUE;
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

    return new ListEventSourceMappingsResult()
        .withEventSourceMappings(copyOf(MAPPINGS.values()));
  }

  @Override
  public GetEventSourceMappingResult getEventSourceMappingConfiguration(String uUID) {
    
    EventSourceMappingConfiguration eventSourceMappingConfiguration = MAPPINGS.get(uUID);
    
    return new GetEventSourceMappingResult()
        .withBatchSize(eventSourceMappingConfiguration.getBatchSize())
        .withEventSourceArn(eventSourceMappingConfiguration.getEventSourceArn())
        .withFunctionArn(eventSourceMappingConfiguration.getFunctionArn())
        .withLastModified(eventSourceMappingConfiguration.getLastModified())
        .withState(eventSourceMappingConfiguration.getState())
        .withUUID(eventSourceMappingConfiguration.getUUID());
  }

  @Override
  public DeleteEventSourceMappingResult deleteEventSourceMappingConfiguration(String uUID) {
    
    EventSourceMappingConfiguration previous = MAPPINGS.remove(uUID);
    
    return new DeleteEventSourceMappingResult()
        .withBatchSize(previous.getBatchSize())
        .withEventSourceArn(previous.getEventSourceArn())
        .withFunctionArn(previous.getFunctionArn())
        .withLastModified(previous.getLastModified())
        .withState(previous.getState())
        .withUUID(previous.getUUID());
  }

  @Override
  public CreateEventSourceMappingResult createEventSourceMapping(CreateEventSourceMappingRequest createEventSourceMappingRequest) {

    String functionArn = functionArn(createEventSourceMappingRequest.getFunctionName());

    if (createEventSourceMappingRequest.getEnabled() == null) {
      createEventSourceMappingRequest.setEnabled(TRUE);
    }

    if (createEventSourceMappingRequest.getBatchSize() == null) {
      createEventSourceMappingRequest.setBatchSize(100);
    }
    
    EventSourceMappingConfiguration eventSourceMappingConfiguration = new EventSourceMappingConfiguration()
        .withUUID(UUID.randomUUID().toString())
        .withBatchSize(createEventSourceMappingRequest.getBatchSize())
        .withEventSourceArn(createEventSourceMappingRequest.getEventSourceArn())
        .withFunctionArn(functionArn)
        .withLastModified(new Date())
        .withState(createEventSourceMappingRequest.getEnabled() ? "Enabled" : "Disabled");

    MAPPINGS.put(eventSourceMappingConfiguration.getUUID(), eventSourceMappingConfiguration);

    return new CreateEventSourceMappingResult()
        .withUUID(eventSourceMappingConfiguration.getUUID())
        .withBatchSize(eventSourceMappingConfiguration.getBatchSize())
        .withEventSourceArn(eventSourceMappingConfiguration.getEventSourceArn())
        .withFunctionArn(eventSourceMappingConfiguration.getFunctionArn())
        .withLastModified(eventSourceMappingConfiguration.getLastModified())
        .withState(eventSourceMappingConfiguration.getState());
  }

  @Override
  public UpdateEventSourceMappingResult updateEventSourceMappingConfiguration(UpdateEventSourceMappingRequest updateEventSourceMappingRequest) {
    
    EventSourceMappingConfiguration eventSourceMappingConfiguration = MAPPINGS.get(updateEventSourceMappingRequest.getUUID());
    
    eventSourceMappingConfiguration.setBatchSize(updateEventSourceMappingRequest.getBatchSize());
    eventSourceMappingConfiguration.withState(updateEventSourceMappingRequest.getEnabled() ? "Enabled" : "Disabled");
    
    MAPPINGS.put(updateEventSourceMappingRequest.getUUID(), eventSourceMappingConfiguration);
    
    return new UpdateEventSourceMappingResult()
        .withBatchSize(eventSourceMappingConfiguration.getBatchSize())
        .withEventSourceArn(eventSourceMappingConfiguration.getEventSourceArn())
        .withFunctionArn(eventSourceMappingConfiguration.getFunctionArn())
        .withLastModified(eventSourceMappingConfiguration.getLastModified())
        .withState(eventSourceMappingConfiguration.getState())
        .withUUID(eventSourceMappingConfiguration.getUUID());
  }
}

package com.digitalsanctum.lambda.server.service;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsRequest;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;

/**
 * @author Shane Witbeck
 * @since 8/23/16
 */
public interface EventSourceMappingService {

  ListEventSourceMappingsResult listEventSourceMappingConfigurations(ListEventSourceMappingsRequest listEventSourceMappingsRequest);

  EventSourceMappingConfiguration getEventSourceMappingConfiguration(String uUID);

  EventSourceMappingConfiguration deleteEventSourceMappingConfiguration(String uUID);

  CreateEventSourceMappingResult createEventSourceMapping(CreateEventSourceMappingRequest createEventSourceMappingRequest);

  EventSourceMappingConfiguration updateEventSourceMappingConfiguration(String uUID, int batchSize, String state);
}

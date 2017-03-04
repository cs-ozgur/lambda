package com.digitalsanctum.lambda.server.service;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.DeleteEventSourceMappingResult;
import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.amazonaws.services.lambda.model.GetEventSourceMappingResult;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsRequest;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import com.amazonaws.services.lambda.model.UpdateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.UpdateEventSourceMappingResult;

/**
 * @author Shane Witbeck
 * @since 8/23/16
 */
public interface EventSourceMappingService {

  ListEventSourceMappingsResult listEventSourceMappingConfigurations(ListEventSourceMappingsRequest listEventSourceMappingsRequest);

  GetEventSourceMappingResult getEventSourceMappingConfiguration(String uUID);

  DeleteEventSourceMappingResult deleteEventSourceMappingConfiguration(String uUID);

  CreateEventSourceMappingResult createEventSourceMapping(CreateEventSourceMappingRequest createEventSourceMappingRequest);

  UpdateEventSourceMappingResult updateEventSourceMappingConfiguration(UpdateEventSourceMappingRequest updateEventSourceMappingRequest);
}

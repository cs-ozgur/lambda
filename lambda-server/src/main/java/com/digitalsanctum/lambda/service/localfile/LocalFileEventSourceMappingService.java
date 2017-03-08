package com.digitalsanctum.lambda.service.localfile;

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
import com.digitalsanctum.lambda.server.util.ArnUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.delete;

/**
 * @author Shane Witbeck
 * @since 3/3/17
 */
public class LocalFileEventSourceMappingService implements EventSourceMappingService {

  private static final Path ROOT_DIR = Paths.get(System.getProperty("user.home"), ".lambda");
  private static final String MAPPING_SUFFIX = "-mappings.json";

  private final LocalFileSystemService localFileSystemService;

  public LocalFileEventSourceMappingService(LocalFileSystemService localFileSystemService) {
    this.localFileSystemService = localFileSystemService;
  }

  @Override
  public ListEventSourceMappingsResult listEventSourceMappingConfigurations(ListEventSourceMappingsRequest listEventSourceMappingsRequest) {
    ListEventSourceMappingsResult result = new ListEventSourceMappingsResult();
    PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + ROOT_DIR + "**/*" + MAPPING_SUFFIX);
    try {
      List<EventSourceMappingConfiguration> eventSourceMappingConfigurations = Files.walk(ROOT_DIR)
          .filter(pathMatcher::matches)
          .map(path -> (EventSourceMappingConfiguration) localFileSystemService.read(path, EventSourceMappingConfiguration.class))
          .collect(Collectors.toList());
      
      result.setEventSourceMappings(eventSourceMappingConfigurations);      

    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public GetEventSourceMappingResult getEventSourceMappingConfiguration(String uUID) {
    Path path = Paths.get(ROOT_DIR.toString(), uUID + MAPPING_SUFFIX);
    EventSourceMappingConfiguration eventSourceMappingConfiguration 
        = (EventSourceMappingConfiguration) localFileSystemService.read(path, EventSourceMappingConfiguration.class);
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
    EventSourceMappingConfiguration eventSourceMappingConfiguration = null;
    try {
      Path path = Paths.get(ROOT_DIR.toString(), uUID + MAPPING_SUFFIX);
      eventSourceMappingConfiguration = (EventSourceMappingConfiguration) localFileSystemService.read(path, EventSourceMappingConfiguration.class);
      delete(path);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new DeleteEventSourceMappingResult()
        .withBatchSize(eventSourceMappingConfiguration.getBatchSize())
        .withEventSourceArn(eventSourceMappingConfiguration.getEventSourceArn())
        .withFunctionArn(eventSourceMappingConfiguration.getFunctionArn())
        .withLastModified(eventSourceMappingConfiguration.getLastModified())
        .withState(eventSourceMappingConfiguration.getState())
        .withUUID(eventSourceMappingConfiguration.getUUID());
  }

  @Override
  public CreateEventSourceMappingResult createEventSourceMapping(CreateEventSourceMappingRequest createEventSourceMappingRequest) {

    String functionArn = ArnUtils.functionArn(createEventSourceMappingRequest.getFunctionName());

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

    Path path = Paths.get(ROOT_DIR.toString(), eventSourceMappingConfiguration.getUUID() + MAPPING_SUFFIX);
    localFileSystemService.write(path, eventSourceMappingConfiguration);

    return new CreateEventSourceMappingResult()
        .withUUID(eventSourceMappingConfiguration.getUUID())
        .withBatchSize(eventSourceMappingConfiguration.getBatchSize())
        .withEventSourceArn(eventSourceMappingConfiguration.getEventSourceArn())
        .withFunctionArn(eventSourceMappingConfiguration.getFunctionArn())
        .withLastModified(eventSourceMappingConfiguration.getLastModified())        
        .withState(createEventSourceMappingRequest.getEnabled() ? "Enabled" : "Disabled");
  }

  @Override
  public UpdateEventSourceMappingResult updateEventSourceMappingConfiguration(UpdateEventSourceMappingRequest updateEventSourceMappingRequest) {
    
    Path path = Paths.get(ROOT_DIR.toString(), updateEventSourceMappingRequest.getUUID() + MAPPING_SUFFIX);    
    
    EventSourceMappingConfiguration eventSourceMappingConfiguration 
        = (EventSourceMappingConfiguration) localFileSystemService.read(path, EventSourceMappingConfiguration.class);    
    
    eventSourceMappingConfiguration.setBatchSize(updateEventSourceMappingRequest.getBatchSize());
    eventSourceMappingConfiguration.setState(updateEventSourceMappingRequest.getEnabled() ? "Enabled" : "Disabled");

    return new UpdateEventSourceMappingResult()
        .withBatchSize(eventSourceMappingConfiguration.getBatchSize())
        .withEventSourceArn(eventSourceMappingConfiguration.getEventSourceArn())
        .withFunctionArn(eventSourceMappingConfiguration.getFunctionArn())
        .withLastModified(eventSourceMappingConfiguration.getLastModified())
        .withState(eventSourceMappingConfiguration.getState())
        .withUUID(eventSourceMappingConfiguration.getUUID());
  }
}

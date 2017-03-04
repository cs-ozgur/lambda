package com.digitalsanctum.lambda.server.service.localfile;

import com.amazonaws.services.lambda.model.CreateEventSourceMappingRequest;
import com.amazonaws.services.lambda.model.CreateEventSourceMappingResult;
import com.amazonaws.services.lambda.model.EventSourceMappingConfiguration;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsRequest;
import com.amazonaws.services.lambda.model.ListEventSourceMappingsResult;
import com.digitalsanctum.lambda.server.service.EventSourceMappingService;
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
          .map(path -> (EventSourceMappingConfiguration) localFileSystemService.read(path.toString(), EventSourceMappingConfiguration.class))
          .collect(Collectors.toList());
      
      result.setEventSourceMappings(eventSourceMappingConfigurations);      

    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public EventSourceMappingConfiguration getEventSourceMappingConfiguration(String uUID) {
    Path path = Paths.get(ROOT_DIR.toString(), uUID + MAPPING_SUFFIX);
    return (EventSourceMappingConfiguration) localFileSystemService.read(path.toString(), EventSourceMappingConfiguration.class);
  }

  @Override
  public EventSourceMappingConfiguration deleteEventSourceMappingConfiguration(String uUID) {
    EventSourceMappingConfiguration eventSourceMappingConfiguration = null;
    try {
      Path path = Paths.get(ROOT_DIR.toString(), uUID + MAPPING_SUFFIX);
      eventSourceMappingConfiguration = (EventSourceMappingConfiguration) localFileSystemService.read(path.toString(), EventSourceMappingConfiguration.class);
      delete(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return eventSourceMappingConfiguration;
  }

  @Override
  public CreateEventSourceMappingResult createEventSourceMapping(CreateEventSourceMappingRequest createEventSourceMappingRequest) {

    String functionArn = ArnUtils.functionArn(createEventSourceMappingRequest.getFunctionName());

    EventSourceMappingConfiguration eventSourceMappingConfiguration = new EventSourceMappingConfiguration()
        .withUUID(UUID.randomUUID().toString())
        .withBatchSize(createEventSourceMappingRequest.getBatchSize())
        .withEventSourceArn(createEventSourceMappingRequest.getEventSourceArn())
        .withFunctionArn(functionArn)
        .withLastModified(new Date())
        .withState("Enabled");

    String path = Paths.get(ROOT_DIR.toString(), eventSourceMappingConfiguration.getUUID() + MAPPING_SUFFIX).toString();
    localFileSystemService.write(path, eventSourceMappingConfiguration);
    
    return new CreateEventSourceMappingResult()
        .withUUID(eventSourceMappingConfiguration.getUUID())
        .withBatchSize(eventSourceMappingConfiguration.getBatchSize())
        .withEventSourceArn(eventSourceMappingConfiguration.getEventSourceArn())
        .withLastModified(eventSourceMappingConfiguration.getLastModified())
        .withState("Enabled");
  }

  @Override
  public EventSourceMappingConfiguration updateEventSourceMappingConfiguration(String uUID, int batchSize, String state) {
    
    Path path = Paths.get(ROOT_DIR.toString(), uUID + MAPPING_SUFFIX);    
    
    EventSourceMappingConfiguration eventSourceMappingConfiguration = (EventSourceMappingConfiguration) localFileSystemService.read(path.toString(), EventSourceMappingConfiguration.class);    
    eventSourceMappingConfiguration.setBatchSize(batchSize);
    eventSourceMappingConfiguration.setState(state);

    return eventSourceMappingConfiguration;
  }
}

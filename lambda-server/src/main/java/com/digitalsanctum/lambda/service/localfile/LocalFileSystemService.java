package com.digitalsanctum.lambda.service.localfile;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author Shane Witbeck
 * @since 3/3/17
 */
public class LocalFileSystemService {
  
  private static final Logger log = LoggerFactory.getLogger(LocalFileSystemService.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  public void write(Path path, byte[] bytes) {
    try {
      Files.write(path, bytes, StandardOpenOption.CREATE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void write(Path path, Object object) {

    try {
      mapper.writeValue(path.toFile(), object);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Object read(Path path, Class<?> clazz) {

    try {
      return mapper.readValue(path.toFile(), clazz);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public boolean delete(Path path) {
    try {
      log.info("deleting {}", path);
      return Files.deleteIfExists(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }
}

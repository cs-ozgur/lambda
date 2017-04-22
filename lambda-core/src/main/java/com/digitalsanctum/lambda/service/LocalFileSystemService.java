package com.digitalsanctum.lambda.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;

/**
 * @author Shane Witbeck
 * @since 3/3/17
 */
public class LocalFileSystemService {
  
  private static final Logger log = LoggerFactory.getLogger(LocalFileSystemService.class);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public void write(Path path, byte[] bytes) {
    try {
      Files.write(path, bytes, CREATE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void write(Path path, Object object) {

    try {
      MAPPER.writeValue(path.toFile(), object);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Object read(Path path, Class<?> clazz) {

    try {
      return MAPPER.readValue(path.toFile(), clazz);
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

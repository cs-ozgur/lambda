package com.digitalsanctum.lambda.service.localfile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author Shane Witbeck
 * @since 3/3/17
 */
public class LocalFileSystemService {

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
}

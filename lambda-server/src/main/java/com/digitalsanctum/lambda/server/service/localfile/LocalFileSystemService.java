package com.digitalsanctum.lambda.server.service.localfile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Shane Witbeck
 * @since 3/3/17
 */
public class LocalFileSystemService {

  private static final ObjectMapper mapper = new ObjectMapper();

  public void write(File file, byte[] bytes) {
    try (FileOutputStream fileOutputStream = new FileOutputStream(file, false)) {
      fileOutputStream.write(bytes);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void write(String path, Object object) {
    try {
      mapper.writeValue(new File(path), object);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Object read(String path, Class<?> clazz) {
    try {
      return mapper.readValue(new File(path), clazz);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}

package com.digitalsanctum.lambda.docker;

/**
 * @author Shane Witbeck
 * @since 8/25/16
 */
public interface DockerService {
  int start();

  void stop();

  boolean isRunning();
  
  int containerPort();
  
  String imageId();
  
  String name();
  
}

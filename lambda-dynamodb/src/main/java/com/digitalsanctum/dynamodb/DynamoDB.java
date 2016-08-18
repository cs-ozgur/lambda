package com.digitalsanctum.dynamodb;

/**
 * @author Shane Witbeck
 * @since 8/17/16
 */
public interface DynamoDB {
  int start();

  void stop();

  boolean isRunning();
}

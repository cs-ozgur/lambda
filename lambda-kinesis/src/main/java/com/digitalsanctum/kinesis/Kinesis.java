package com.digitalsanctum.kinesis;

/**
 * @author Shane Witbeck
 * @since 8/16/16
 */
public interface Kinesis {

  int start();

  void stop();

  boolean isRunning();
  
}

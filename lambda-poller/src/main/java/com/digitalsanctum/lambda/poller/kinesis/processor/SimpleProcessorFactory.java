package com.digitalsanctum.lambda.poller.kinesis.processor;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;


/**
 * @author Shane Witbeck
 * @since 4/3/16
 */
public class SimpleProcessorFactory implements IRecordProcessorFactory {
  
  @Override
  public IRecordProcessor createProcessor() {
    return new SimpleRecordProcessor();
  }
}

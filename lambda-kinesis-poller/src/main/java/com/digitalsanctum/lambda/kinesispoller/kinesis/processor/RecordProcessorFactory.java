package com.digitalsanctum.lambda.kinesispoller.kinesis.processor;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;


/**
 * @author Shane Witbeck
 * @since 4/3/16
 */
public class RecordProcessorFactory implements IRecordProcessorFactory {
  
  private String lambdaServerEndpoint;

  public RecordProcessorFactory(String lambdaServerEndpoint) {
    super();
    this.lambdaServerEndpoint = lambdaServerEndpoint;
  }

  @Override
  public IRecordProcessor createProcessor() {
    return new RecordProcessor(this.lambdaServerEndpoint);
  }
}

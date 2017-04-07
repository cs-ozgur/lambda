package com.digitalsanctum.lambda.poller.kinesis.processor;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;

/**
 * @author Shane Witbeck
 * @since 3/31/17
 */
public class LambdaInvokingProcessorFactory implements IRecordProcessorFactory {

  private final AwsClientBuilder.EndpointConfiguration lambdaEndpointConfiguration;
  private final String functionName;

  public LambdaInvokingProcessorFactory(AwsClientBuilder.EndpointConfiguration lambdaEndpointConfiguration,
                                        String functionName) {
    this.lambdaEndpointConfiguration = lambdaEndpointConfiguration;
    this.functionName = functionName;
  }

  @Override public IRecordProcessor createProcessor() {
    return new LambdaInvokingRecordProcessor(lambdaEndpointConfiguration, functionName);
  }
}

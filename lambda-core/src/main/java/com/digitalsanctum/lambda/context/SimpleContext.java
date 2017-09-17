package com.digitalsanctum.lambda.context;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.digitalsanctum.lambda.logging.SystemOutLambaLogger;

public class SimpleContext implements Context {

    private final String functionName;
    private final String awsRequestId;

    public SimpleContext(String awsRequestId, String functionName) {
        this.awsRequestId = awsRequestId;
        this.functionName = functionName;
    }

    public String getAwsRequestId() {
        return this.awsRequestId;
    }

    public String getLogGroupName() {
        return "local-" + getFunctionName();
    }

    public String getLogStreamName() {
        return getLogGroupName() + getAwsRequestId();
    }

    public String getFunctionName() {
        return this.functionName;
    }

    @Override
    public String getFunctionVersion() {
        return "local";
    }

    @Override
    public String getInvokedFunctionArn() {
        return getFunctionVersion() + ":" + getFunctionName();
    }

    public CognitoIdentity getIdentity() {
        throw new UnsupportedOperationException();
    }

    public ClientContext getClientContext() {
        return new SimpleClientContext();
    }

    public int getRemainingTimeInMillis() {
        return Integer.MAX_VALUE;
    }

    public int getMemoryLimitInMB() {
        return Integer.MAX_VALUE;
    }

    public LambdaLogger getLogger() {
        return new SystemOutLambaLogger();
    }
}

package com.digitalsanctum.lambda.kinesispoller.kinesis.config;

import com.amazonaws.ClientConfiguration;

public class ConfigurationUtils {

    private static final String APPLICATION_NAME = "kinesis-poller";
    private static final String VERSION = "1.0.0";
    
    public static String getApplicationName() {
        return APPLICATION_NAME;
    }

    public static ClientConfiguration getClientConfigWithUserAgent() {
        final ClientConfiguration config = new ClientConfiguration();
        final StringBuilder userAgent = new StringBuilder(ClientConfiguration.DEFAULT_USER_AGENT);

        // Separate fields of the user agent with a space
        userAgent.append(" ");
        // Append the application name followed by version number of the sample
        userAgent.append(APPLICATION_NAME);
        userAgent.append("/");
        userAgent.append(VERSION);

        config.setUserAgent(userAgent.toString());

        return config;
    }
}

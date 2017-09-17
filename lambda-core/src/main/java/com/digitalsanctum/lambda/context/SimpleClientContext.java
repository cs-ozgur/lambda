package com.digitalsanctum.lambda.context;

import com.amazonaws.services.lambda.runtime.Client;
import com.amazonaws.services.lambda.runtime.ClientContext;

import java.util.Collections;
import java.util.Map;

/**
 * @author ocicek
 * @version 11/09/2017
 */
public class SimpleClientContext implements ClientContext {
    public SimpleClientContext() {
    }

    public Client getClient() {
        return null;
    }

    public Map<String, String> getCustom() {
        return Collections.EMPTY_MAP;
    }

    public Map<String, String> getEnvironment() {
        return Collections.EMPTY_MAP;
    }
}

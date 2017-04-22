package com.digitalsanctum.lambda.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Shane Witbeck
 * @since 4/17/17
 */
public class LocalUtils {

  private static final String HOST = "localhost";

  public static String localEndpoint(int port) {
    String endpoint = null;
    try {
      endpoint = new URL("http", HOST, port, "").toString();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    return endpoint;
  }
}

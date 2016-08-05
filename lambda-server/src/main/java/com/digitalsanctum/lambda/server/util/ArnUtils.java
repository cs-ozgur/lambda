package com.digitalsanctum.lambda.server.util;

/**
 * @author Shane Witbeck
 * @since 7/23/16
 */
public class ArnUtils {
  
  public static String of(String functionName) {                                      
    return "arn:aws:lambda:local:111000111000:function:" + functionName;
  }
}

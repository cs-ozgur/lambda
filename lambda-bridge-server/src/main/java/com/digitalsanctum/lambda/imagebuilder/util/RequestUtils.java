package com.digitalsanctum.lambda.imagebuilder.util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;

/**
 * @author Shane Witbeck
 * @since 8/10/16
 */
public class RequestUtils {
  
  public static String readRequestJson(HttpServletRequest req) {
    StringBuilder sb = new StringBuilder();
    String line;
    try {
      BufferedReader reader = req.getReader();
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sb.toString();
  }
}

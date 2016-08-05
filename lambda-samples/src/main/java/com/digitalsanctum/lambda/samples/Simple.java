package com.digitalsanctum.lambda.samples;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

/**
 * @author Shane Witbeck
 * @since 8/4/16
 */
public class Simple {
  
  public void foo(String input) {
    System.out.println("input=" + input);
  }
  
  public void bar(String input, Context context) {
    LambdaLogger log = context.getLogger();
    log.log("input=" + input);
  }
  
  public void pojo(TestRequest testRequest) {
    System.out.println(testRequest);
  }
}

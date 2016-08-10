package com.digitalsanctum.lambda.model;

import java.nio.ByteBuffer;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class CreateImageRequest {
  
  private String imageName;
  private ByteBuffer lambdaJar;

  public String getImageName() {
    return imageName;
  }

  public void setImageName(String imageName) {
    this.imageName = imageName;
  }

  public ByteBuffer getLambdaJar() {
    return lambdaJar;
  }

  public void setLambdaJar(ByteBuffer lambdaJar) {
    this.lambdaJar = lambdaJar;
  }
}

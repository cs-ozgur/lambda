package com.digitalsanctum.lambda.model;

import java.nio.ByteBuffer;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class CreateImageRequest {
  
  private String imageName;
  private ByteBuffer lambdaJar;

  public CreateImageRequest() {
  }

  public CreateImageRequest(String imageName) {
    this.imageName = imageName;
  }

  public CreateImageRequest(String imageName, ByteBuffer lambdaJar) {
    this.imageName = imageName;
    this.lambdaJar = lambdaJar;
  }

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

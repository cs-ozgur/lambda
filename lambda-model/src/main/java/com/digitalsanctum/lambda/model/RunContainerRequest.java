package com.digitalsanctum.lambda.model;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class RunContainerRequest {
  private String imageId;
  private String handler;

  public String getImageId() {
    return imageId;
  }

  public void setImageId(String imageId) {
    this.imageId = imageId;
  }

  public String getHandler() {
    return handler;
  }

  public void setHandler(String handler) {
    this.handler = handler;
  }
}

package com.digitalsanctum.lambda.model;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class RunContainerRequest {
  private String imageId;
  private String handler;
  private String name;

  public RunContainerRequest() {
  }

  public RunContainerRequest(String imageId, String handler, String name) {
    this.imageId = imageId;
    this.handler = handler;
    this.name = name;
  }

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RunContainerRequest that = (RunContainerRequest) o;

    return Objects.equals(this.handler, that.handler) &&
        Objects.equals(this.imageId, that.imageId) &&
        Objects.equals(this.name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(handler, imageId, name);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("handler = " + handler)
        .add("imageId = " + imageId)
        .add("name = " + name)
        .toString();
  }
}

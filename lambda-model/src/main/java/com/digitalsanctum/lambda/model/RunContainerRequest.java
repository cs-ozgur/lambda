package com.digitalsanctum.lambda.model;

import java.util.Map;
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
  private Map<String, String> environmentVariables;

  public RunContainerRequest() {
  }

  public RunContainerRequest(String imageId, String handler, String name, Map<String, String> environmentVariables) {
    this.imageId = imageId;
    this.handler = handler;
    this.name = name;
    this.environmentVariables = environmentVariables;
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

  public Map<String, String> getEnvironmentVariables() {
    return environmentVariables;
  }

  public void setEnvironmentVariables(Map<String, String> environmentVariables) {
    this.environmentVariables = environmentVariables;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    RunContainerRequest that = (RunContainerRequest) o;

    return Objects.equals(this.handler, that.handler) &&
            Objects.equals(this.imageId, that.imageId) &&
            Objects.equals(this.name, that.name) &&
            Objects.equals(this.environmentVariables, that.environmentVariables);
  }

  @Override
  public int hashCode() {
    return Objects.hash(handler, imageId, name, environmentVariables);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
            .add("handler = " + handler)
            .add("imageId = " + imageId)
            .add("name = " + name)
            .add("environmentVariables = " + environmentVariables)
            .toString();
  }
}

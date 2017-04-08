package com.digitalsanctum.lambda.model;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Shane Witbeck
 * @since 4/7/17
 */
public class DeleteContainerRequest {
  private String containerId;

  public DeleteContainerRequest(String containerId) {
    this.containerId = containerId;
  }

  public String getContainerId() {
    return containerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DeleteContainerRequest that = (DeleteContainerRequest) o;

    return Objects.equals(this.containerId, that.containerId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(containerId);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("containerId = " + containerId)
        .toString();
  }
}

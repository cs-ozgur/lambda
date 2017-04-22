package com.digitalsanctum.lambda.model;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Shane Witbeck
 * @since 4/18/17
 */
public class FunctionContainerConfiguration {
  private String functionName;
  private String containerId;

  public FunctionContainerConfiguration() {
  }

  public FunctionContainerConfiguration(String functionName, String containerId) {
    this.functionName = functionName;
    this.containerId = containerId;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

  public String getContainerId() {
    return containerId;
  }

  public void setContainerId(String containerId) {
    this.containerId = containerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FunctionContainerConfiguration that = (FunctionContainerConfiguration) o;

    return Objects.equals(this.containerId, that.containerId) &&
        Objects.equals(this.functionName, that.functionName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(containerId, functionName);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("containerId = " + containerId)
        .add("functionName = " + functionName)
        .toString();
  }
}

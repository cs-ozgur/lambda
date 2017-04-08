package com.digitalsanctum.lambda.model;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Shane Witbeck
 * @since 4/7/17
 */
public class ListContainersResponse {
  private int statusCode;
  private String errorMessage;
  private List<Container> containers;
  
  public ListContainersResponse() {
  }

  public ListContainersResponse(int statusCode, String errorMessage) {
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
  }

  public ListContainersResponse(int statusCode, List<Container> containers) {
    this.statusCode = statusCode;
    this.containers = containers;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public List<Container> getContainers() {
    return containers;
  }

  public void setContainers(List<Container> containers) {
    this.containers = containers;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ListContainersResponse that = (ListContainersResponse) o;

    return Objects.equals(this.containers, that.containers) &&
        Objects.equals(this.errorMessage, that.errorMessage) &&
        Objects.equals(this.statusCode, that.statusCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(containers, errorMessage, statusCode);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("containers = " + containers)
        .add("errorMessage = " + errorMessage)
        .add("statusCode = " + statusCode)
        .toString();
  }
}

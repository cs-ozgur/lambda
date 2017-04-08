package com.digitalsanctum.lambda.model;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Shane Witbeck
 * @since 4/7/17
 */
public class DeleteContainerResponse {
  
  private int statusCode;
  private String errorMessage;

  public DeleteContainerResponse() {
  }

  public DeleteContainerResponse(int statusCode) {
    this.statusCode = statusCode;
  }

  public DeleteContainerResponse(int statusCode, String errorMessage) {
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DeleteContainerResponse that = (DeleteContainerResponse) o;

    return Objects.equals(this.errorMessage, that.errorMessage) &&
        Objects.equals(this.statusCode, that.statusCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errorMessage, statusCode);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("errorMessage = " + errorMessage)
        .add("statusCode = " + statusCode)
        .toString();
  }
}

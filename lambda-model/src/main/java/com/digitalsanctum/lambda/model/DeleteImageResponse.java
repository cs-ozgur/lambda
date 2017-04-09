package com.digitalsanctum.lambda.model;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Shane Witbeck
 * @since 4/9/17
 */
public class DeleteImageResponse {

  private int statusCode;
  private String errorMessage;

  public DeleteImageResponse(int statusCode) {
    this.statusCode = statusCode;
  }

  public DeleteImageResponse(int statusCode, String errorMessage) {
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
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


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DeleteImageResponse that = (DeleteImageResponse) o;

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

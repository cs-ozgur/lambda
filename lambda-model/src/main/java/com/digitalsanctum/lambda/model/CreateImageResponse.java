package com.digitalsanctum.lambda.model;

import java.util.Objects;
import java.util.StringJoiner;

import static com.digitalsanctum.lambda.model.HttpStatus.SC_CREATED;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class CreateImageResponse {
  private String imageId;
  private int statusCode;
  private String errorMessage;

  public CreateImageResponse() {
  }

  public CreateImageResponse(String imageId) {
    this.imageId = imageId;
    this.statusCode = SC_CREATED;
  }

  public CreateImageResponse(int statusCode, String errorMessage) {
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
  }

  public String getImageId() {
    return imageId;
  }

  public void setImageId(String imageId) {
    this.imageId = imageId;
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

    CreateImageResponse that = (CreateImageResponse) o;

    return Objects.equals(this.errorMessage, that.errorMessage) &&
        Objects.equals(this.imageId, that.imageId) &&
        Objects.equals(this.statusCode, that.statusCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errorMessage, imageId, statusCode);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("errorMessage = " + errorMessage)
        .add("imageId = " + imageId)
        .add("statusCode = " + statusCode)
        .toString();
  }
}

package com.digitalsanctum.lambda.model;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Shane Witbeck
 * @since 4/9/17
 */
public class GetImageResponse {
  private Image image;
  private int statusCode;
  private String errorMessage;

  public GetImageResponse() {
  }

  public GetImageResponse(int statusCode, Image image) {
    this.statusCode = statusCode;
    this.image = image;
  }

  public GetImageResponse(int statusCode, String errorMessage) {
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
  }

  public Image getImage() {
    return image;
  }

  public void setImage(Image image) {
    this.image = image;
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

    GetImageResponse that = (GetImageResponse) o;

    return Objects.equals(this.errorMessage, that.errorMessage) &&
        Objects.equals(this.image, that.image) &&
        Objects.equals(this.statusCode, that.statusCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errorMessage, image, statusCode);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("errorMessage = " + errorMessage)
        .add("image = " + image)
        .add("statusCode = " + statusCode)
        .toString();
  }
}

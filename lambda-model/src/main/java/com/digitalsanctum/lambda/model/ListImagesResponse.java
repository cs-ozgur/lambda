package com.digitalsanctum.lambda.model;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static com.digitalsanctum.lambda.model.HttpStatus.SC_OK;

/**
 * @author Shane Witbeck
 * @since 4/9/17
 */
public class ListImagesResponse {

  private int statusCode;
  private String errorMessage;
  private List<Image> images;

  public ListImagesResponse() {
  }

  public ListImagesResponse(List<Image> images) {
    this.statusCode = SC_OK;
    this.images = images;
  }

  public ListImagesResponse(int statusCode, String errorMessage) {
    this.statusCode = statusCode;
    this.errorMessage = errorMessage;
  }

  public List<Image> getImages() {
    return images;
  }

  public void setImages(List<Image> images) {
    this.images = images;
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

    ListImagesResponse that = (ListImagesResponse) o;

    return Objects.equals(this.errorMessage, that.errorMessage) &&
        Objects.equals(this.images, that.images) &&
        Objects.equals(this.statusCode, that.statusCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errorMessage, images, statusCode);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("errorMessage = " + errorMessage)
        .add("images = " + images)
        .add("statusCode = " + statusCode)
        .toString();
  }
}

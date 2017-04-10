package com.digitalsanctum.lambda.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Shane Witbeck
 * @since 4/9/17
 */
public class Image {
  private String id;
  private Map<String, String> labels;
  private List<String> tags;

  public Image() {
  }

  public Image(String id, Map<String, String> labels, List<String> tags) {
    this.id = id;
    this.labels = labels;
    this.tags = tags;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Image that = (Image) o;

    return Objects.equals(this.id, that.id) &&
        Objects.equals(this.labels, that.labels) &&
        Objects.equals(this.tags, that.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, labels, tags);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("id = " + id)
        .add("labels = " + labels)
        .add("tags = " + tags)
        .toString();
  }
}

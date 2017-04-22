package com.digitalsanctum.lambda.model;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * @author Shane Witbeck
 * @since 4/7/17
 */
public class Container {
  private String id;
  private String name;

  public Container(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public Container() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Container that = (Container) o;

    return Objects.equals(this.id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
        .add("id = " + id)
        .add("name = " + name)
        .toString();
  }
}

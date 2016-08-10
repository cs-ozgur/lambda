package com.digitalsanctum.lambda.model;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class RunContainerResult {
  
  private String name;
  private String hostname;
  private String endpoint;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }
}

package com.digitalsanctum.dynamodb;

import com.digitalsanctum.lambda.docker.AbstractDockerService;

/**
 * @author Shane Witbeck
 * @since 8/17/16
 */
public class DockerDynamoDB extends AbstractDockerService {
  
  @Override
  public String name() {
    return "dynamodb";
  }

  @Override
  public String imageId() {
    return "peopleperhour/dynamodb";
  }

  @Override
  public int containerPort() {
    return 8000;
  }
}

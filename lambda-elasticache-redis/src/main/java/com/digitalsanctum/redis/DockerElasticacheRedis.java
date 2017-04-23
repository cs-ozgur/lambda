package com.digitalsanctum.redis;

import com.digitalsanctum.lambda.docker.AbstractDockerService;

/**
 * NOTE: This only provides a single Redis node but does not implement the AWS Elasticache API.
 *
 * @author Shane Witbeck
 * @since 4/14/17
 */
public class DockerElasticacheRedis extends AbstractDockerService {

  @Override
  public String name() {
    return "elasticache-redis";
  }

  @Override
  public String imageId() {
    return "redis:latest";
  }

  @Override
  public int containerPort() {
    return 6379;
  }
}

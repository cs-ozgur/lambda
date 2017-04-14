package com.digitalsanctum.s3;

import com.digitalsanctum.lambda.docker.AbstractDockerService;

/**
 * @author Shane Witbeck
 * @since 4/14/17
 */
public class DockerS3 extends AbstractDockerService {

  @Override
  public String name() {
    return "S3";
  }

  @Override
  public String imageId() {
    return "lphoward/fake-s3";
  }

  @Override
  public int containerPort() {
    return 4569;
  }
}

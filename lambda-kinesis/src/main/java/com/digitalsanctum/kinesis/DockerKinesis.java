package com.digitalsanctum.kinesis;

import com.digitalsanctum.lambda.docker.AbstractDockerService;

/**
 * @author Shane Witbeck
 * @since 8/19/16
 */
public class DockerKinesis extends AbstractDockerService {

  @Override
  public int containerPort() {
    return 4567;
  }

  @Override
  public String imageId() {
    return "instructure/kinesalite";
  }

  @Override
  public String name() {
    return "kinesis";
  }
}

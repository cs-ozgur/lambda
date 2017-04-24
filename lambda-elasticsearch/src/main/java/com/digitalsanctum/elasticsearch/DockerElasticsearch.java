package com.digitalsanctum.elasticsearch;

import com.digitalsanctum.lambda.docker.AbstractDockerService;

/**
 * Opting here to use version 5.3.1 instead of the latest supported version by AWS (5.1).
 * <p>
 * For reference, see: https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html
 *
 * @author Shane Witbeck
 * @since 4/23/17
 */
public class DockerElasticsearch extends AbstractDockerService {

  @Override
  public String name() {
    return "elasticsearch";
  }

  @Override
  public String imageId() {
    return "docker.elastic.co/elasticsearch/elasticsearch:5.3.1";
  }

  @Override
  public int containerPort() {
    return 9200;
  }
}

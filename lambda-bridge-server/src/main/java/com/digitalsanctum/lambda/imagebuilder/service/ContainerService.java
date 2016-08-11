package com.digitalsanctum.lambda.imagebuilder.service;

import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResult;
import com.spotify.docker.client.exceptions.DockerException;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public interface ContainerService {
  RunContainerResult createAndRunContainer(RunContainerRequest runContainerRequest) throws DockerException, InterruptedException;
}

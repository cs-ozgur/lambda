package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.DeleteContainerRequest;
import com.digitalsanctum.lambda.model.DeleteContainerResponse;
import com.digitalsanctum.lambda.model.ListContainersResponse;
import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResponse;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public interface ContainerService {
  RunContainerResponse createAndRunContainer(RunContainerRequest runContainerRequest);  
  DeleteContainerResponse deleteContainer(DeleteContainerRequest deleteContainerRequest);
  ListContainersResponse listContainers();
}

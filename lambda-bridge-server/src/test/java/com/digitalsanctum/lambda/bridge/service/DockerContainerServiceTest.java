package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;
import com.digitalsanctum.lambda.model.DeleteContainerRequest;
import com.digitalsanctum.lambda.model.DeleteContainerResponse;
import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResponse;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.digitalsanctum.lambda.model.HttpStatus.SC_CREATED;
import static com.digitalsanctum.lambda.model.HttpStatus.SC_OK;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class DockerContainerServiceTest {

  private ContainerService containerService;
  private String containerId;

  @Before
  public void setup() throws Exception {
    final DockerClient dockerClient = DefaultDockerClient.fromEnv().build();
    DockerImageService dockerImageService = new DockerImageService(dockerClient);
    containerService = new DockerContainerService(dockerClient);

    CreateImageRequest request = new CreateImageRequest();
    request.setImageName("test");

    InputStream is = DockerImageBuilderTest.class.getClassLoader().getResourceAsStream("lambda.jar");
    byte[] bytes = IOUtils.toByteArray(is);
    ByteBuffer lambdaByteBuffer = ByteBuffer.wrap(bytes);
    request.setLambdaJar(lambdaByteBuffer);

    CreateImageResponse result = dockerImageService.createImage(request);
    assertNotNull(result);
    assertNotNull(result.getImageId());
  }
  
  @After
  public void tearDown() throws Exception {
    DeleteContainerResponse deleteContainerResponse = containerService.deleteContainer(new DeleteContainerRequest(containerId));
    assertThat(deleteContainerResponse.getStatusCode(), is(SC_OK));
    assertNull(deleteContainerResponse.getErrorMessage());
  }

  @Test
  public void testCreateAndRunContainer() throws Exception {

    RunContainerRequest request = new RunContainerRequest();
    request.setImageId("test");
    request.setHandler("com.digitalsanctum.lambda.functions.Concat");
    
    RunContainerResponse runContainerResponse = containerService.createAndRunContainer(request);
    containerId = runContainerResponse.getContainerId();
    
    assertThat(runContainerResponse.getStatusCode(), is(SC_CREATED));
    assertNull(runContainerResponse.getErrorMessage());
    assertNotNull(runContainerResponse.getName());
    assertNotNull(runContainerResponse.getHostname());
    assertNotNull(runContainerResponse.getEndpoint());
  }
}

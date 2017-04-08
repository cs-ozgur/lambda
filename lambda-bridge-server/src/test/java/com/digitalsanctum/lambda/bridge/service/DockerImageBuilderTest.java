package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertNotNull;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class DockerImageBuilderTest {

  private DockerImageBuilder dockerImageBuilder;

  @Before
  public void setup() throws Exception {
    final DockerClient dockerClient = DefaultDockerClient.fromEnv().build();
    dockerImageBuilder = new DockerImageBuilder(dockerClient);
  }

  @Test
  public void testCreateFunctionImage() throws Exception {

    CreateImageRequest request = new CreateImageRequest();
    request.setImageName("test");

    // read test jar into ByteBuffer
    InputStream is = DockerImageBuilderTest.class.getClassLoader().getResourceAsStream("lambda.jar");
    byte[] bytes = IOUtils.toByteArray(is);
    ByteBuffer lambdaByteBuffer = ByteBuffer.wrap(bytes);
    request.setLambdaJar(lambdaByteBuffer);
    
    CreateImageResponse result = dockerImageBuilder.createImage(request);
    assertNotNull(result);
    assertNotNull(result.getImageId());
  }
}

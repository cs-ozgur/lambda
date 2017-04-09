package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;
import com.digitalsanctum.lambda.model.DeleteImageResponse;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class DockerImageBuilderTest {

  private DockerImageService dockerImageService;
  private String imageId;

  @Before
  public void setup() throws Exception {
    final DockerClient dockerClient = DefaultDockerClient.fromEnv().build();
    dockerImageService = new DockerImageService(dockerClient);
  }
  
  @After
  public void tearDown() throws Exception {
    if (imageId != null) {
      DeleteImageResponse deleteImageResponse = dockerImageService.deleteImage(imageId);
      assertThat(deleteImageResponse.getStatusCode(), is(OK_200));
    }    
  }

  @Test
  public void testCreateFunctionImage() throws Exception {

    // read test jar into ByteBuffer
    InputStream is = DockerImageBuilderTest.class.getClassLoader().getResourceAsStream("lambda.jar");
    byte[] bytes = IOUtils.toByteArray(is);
    ByteBuffer lambdaByteBuffer = ByteBuffer.wrap(bytes);

    CreateImageRequest request = new CreateImageRequest("test", lambdaByteBuffer);
    
    CreateImageResponse result = dockerImageService.createImage(request);

    assertNotNull(result);
    assertThat(result.getStatusCode(), is(CREATED_201));
    assertNotNull(result.getImageId());

    this.imageId = result.getImageId();
  }
}

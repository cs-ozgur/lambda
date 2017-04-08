package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;
import com.google.common.io.Files;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 8/8/16
 */
public class DockerImageService implements ImageService<Image> {
  
  private static final Logger log = LoggerFactory.getLogger(DockerImageService.class);
  
  private final DockerClient dockerClient;

  public DockerImageService(DockerClient dockerClient) {
    this.dockerClient = dockerClient;
  }

  @Override
  public List<Image> getFunctionImages() {
    try {      
      return dockerClient.listImages();      
    } catch (DockerException | InterruptedException e) {
      e.printStackTrace();
    }
    return Collections.emptyList();
  }

  @Override
  public CreateImageResponse createImage(CreateImageRequest createImageRequest) {
    // create tmp dir
    File tmpDir = Files.createTempDir();

    // copy proxy.jar to tmp dir
    InputStream proxyIs = DockerImageService.class.getClassLoader().getResourceAsStream("proxy.jar");
    try {
      java.nio.file.Files.copy(proxyIs, tmpDir.toPath().resolve("proxy.jar"));
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        proxyIs.close();
      } catch (IOException e) {
        // noop
      }
    }

    // copy lambda.jar to tmp dir
    Path file = tmpDir.toPath().resolve("lambda.jar");
    FileChannel wChannel;
    try {
      wChannel = new FileOutputStream(file.toFile(), false).getChannel();
      wChannel.write(createImageRequest.getLambdaJar());
    } catch (IOException e) {
      e.printStackTrace();
    }

    // copy Dockerfile to tmp dir
    InputStream dockerfileStream = DockerImageService.class.getClassLoader().getResourceAsStream("Dockerfile");
    try {
      java.nio.file.Files.copy(dockerfileStream, Paths.get(tmpDir.getAbsolutePath(), "Dockerfile"));
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        dockerfileStream.close();
      } catch (IOException e) {
        // noop
      }
    }

    // create the image via Docker remote api
    String id = null;
    try {
      id = dockerClient.build(tmpDir.toPath(), createImageRequest.getImageName());
    } catch (DockerException de) {
      log.error("Error creating Docker image '{}'. Error: {}", createImageRequest.getImageName(), de.getMessage(), de);
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
    }

    CreateImageResponse result = new CreateImageResponse();
    result.setImageId(id);

    return result;
  }
}

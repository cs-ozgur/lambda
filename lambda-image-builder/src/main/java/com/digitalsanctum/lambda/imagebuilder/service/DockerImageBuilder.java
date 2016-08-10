package com.digitalsanctum.lambda.imagebuilder.service;

import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResult;
import com.google.common.io.Files;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Image;
import com.sun.tools.doclets.internal.toolkit.util.DocFinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 8/8/16
 */
public class DockerImageBuilder implements ImageBuilder<Image> {
  
  private final DockerClient dockerClient;

  public DockerImageBuilder(DockerClient dockerClient) {
    this.dockerClient = dockerClient;
  }

  @Override
  public Image getFunctionImage(String id) {
    return null;
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
  public CreateImageResult createImage(CreateImageRequest createImageRequest) {
    // create tmp dir
    File tmpDir = Files.createTempDir();
    System.out.println("created " + tmpDir.getAbsolutePath());

    // copy proxy.jar to tmp dir
    InputStream proxyIs = DockerImageBuilder.class.getClassLoader().getResourceAsStream("proxy.jar");
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
    FileChannel wChannel = null;
    try {
      wChannel = new FileOutputStream(file.toFile(), false).getChannel();
      wChannel.write(createImageRequest.getLambdaJar());
    } catch (IOException e) {
      e.printStackTrace();
    }

    // copy Dockerfile to tmp dir
    InputStream dockerfileStream = DockerImageBuilder.class.getClassLoader().getResourceAsStream("Dockerfile");
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
    } catch (DockerException | InterruptedException | IOException e) {
      e.printStackTrace();
    }

    CreateImageResult result = new CreateImageResult();
    result.setImageId(id);

    return result;
  }

  @Override
  public void updateFunctionImage() {
    // TODO same as createImage ?
  }

  @Override
  public void invokeFunctionImage() {
    // TODO verify container exists. if not, then call run
    // TODO capture endpoint/port via inspect?
    // TODO call endpoint with input
    // TODO capture output
    // TODO return meta and output
  }
}

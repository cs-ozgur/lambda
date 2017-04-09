package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;
import com.digitalsanctum.lambda.model.DeleteImageResponse;
import com.digitalsanctum.lambda.model.ListImagesResponse;
import com.google.common.io.Files;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static com.digitalsanctum.lambda.model.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static com.digitalsanctum.lambda.model.HttpStatus.SC_NOT_FOUND;
import static com.digitalsanctum.lambda.model.HttpStatus.SC_OK;
import static java.lang.String.format;
import static java.nio.file.Files.copy;

/**
 * @author Shane Witbeck
 * @since 8/8/16
 */
public class DockerImageService implements ImageService {

  private static final Logger log = LoggerFactory.getLogger(DockerImageService.class);
  
  private static final String DOCKERFILE = "Dockerfile";
  private static final String LAMBDA_JAR = "lambda.jar";
  private static final String PROXY_JAR = "proxy.jar";

  private final DockerClient dockerClient;

  public DockerImageService(DockerClient dockerClient) {
    this.dockerClient = dockerClient;
  }

  @Override
  public ListImagesResponse listImages() {
    try {
      List<com.digitalsanctum.lambda.model.Image> images = dockerClient.listImages().stream()
          .map(image -> new com.digitalsanctum.lambda.model.Image(image.id()))
          .collect(Collectors.toList());
      return new ListImagesResponse(images);
    } catch (DockerException | InterruptedException e) {
      return new ListImagesResponse(SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @Override
  public CreateImageResponse createImage(CreateImageRequest createImageRequest) {
    
    try {
      ClassLoader classLoader = DockerImageService.class.getClassLoader();

      // create tmp dir
      File tmpDir = Files.createTempDir();
      Path tmpDirPath = tmpDir.toPath();
      
      // copy proxy.jar to tmp dir
      InputStream proxyIs = classLoader.getResourceAsStream(PROXY_JAR);
      copy(proxyIs, tmpDirPath.resolve(PROXY_JAR));
      proxyIs.close();

      // copy lambda.jar to tmp dir
      Path lambdaJarPath = tmpDirPath.resolve(LAMBDA_JAR);
      FileChannel wChannel = new FileOutputStream(lambdaJarPath.toFile(), false).getChannel();
      wChannel.write(createImageRequest.getLambdaJar());
      wChannel.close();

      // copy Dockerfile to tmp dir
      InputStream dockerfileStream = classLoader.getResourceAsStream(DOCKERFILE);
      copy(dockerfileStream, Paths.get(tmpDir.getAbsolutePath(), DOCKERFILE));
      dockerfileStream.close();

      // create the image via Docker remote api
      String imageName = createImageRequest.getImageName();
      log.info("building '{}' image from path: {}", imageName, tmpDirPath);
      String imageId = dockerClient.build(tmpDirPath, imageName);
      return new CreateImageResponse(imageId);
      
    } catch (DockerException | InterruptedException | IOException e) {
      log.error(format("Error building image '%s'", createImageRequest.getImageName()), e);
      return new CreateImageResponse(SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } 
  }

  @Override public DeleteImageResponse deleteImage(String imageId) {
    try {
      dockerClient.removeImage(imageId, true, false);
      return new DeleteImageResponse(SC_OK);
    } catch (ImageNotFoundException infe) {
      log.error(format("Image not found: %s", imageId), infe);
      return new DeleteImageResponse(SC_NOT_FOUND, infe.getMessage());
    } catch (DockerException | InterruptedException e) {
      log.error(format("Error deleting image: %s", imageId), e);
      return new DeleteImageResponse(SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}

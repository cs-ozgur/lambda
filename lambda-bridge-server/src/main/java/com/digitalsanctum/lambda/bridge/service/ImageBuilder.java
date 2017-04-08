package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;

import java.util.List;

/**
 * @author Shane Witbeck
 * @since 8/7/16
 */
public interface ImageBuilder<T> {
  
  List<T> getFunctionImages();

  CreateImageResponse createImage(CreateImageRequest createImageRequest);
}

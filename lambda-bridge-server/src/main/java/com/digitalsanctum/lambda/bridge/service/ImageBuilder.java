package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResult;

import java.util.List;

/**
 * @author Shane Witbeck
 * @since 8/7/16
 */
public interface ImageBuilder<T> {
  
  List<T> getFunctionImages();

  CreateImageResult createImage(CreateImageRequest createImageRequest);
}

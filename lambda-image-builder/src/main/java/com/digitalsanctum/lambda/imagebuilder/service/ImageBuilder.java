package com.digitalsanctum.lambda.imagebuilder.service;

import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResult;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Shane Witbeck
 * @since 8/7/16
 */
public interface ImageBuilder<T> {
  
  T getFunctionImage(String id);
  
  List<T> getFunctionImages();

  CreateImageResult createImage(CreateImageRequest createImageRequest);
  
  void updateFunctionImage();
  
  void invokeFunctionImage();
}

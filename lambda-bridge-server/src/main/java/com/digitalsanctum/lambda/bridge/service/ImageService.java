package com.digitalsanctum.lambda.bridge.service;

import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;
import com.digitalsanctum.lambda.model.DeleteImageResponse;
import com.digitalsanctum.lambda.model.ListImagesResponse;

/**
 * @author Shane Witbeck
 * @since 8/7/16
 */
public interface ImageService {

  ListImagesResponse listImages();

  CreateImageResponse createImage(CreateImageRequest createImageRequest);
  
  DeleteImageResponse deleteImage(String imageId);
}

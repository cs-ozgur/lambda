package com.digitalsanctum.lambda.bridge.servlet;

import com.digitalsanctum.lambda.bridge.service.ImageService;
import com.digitalsanctum.lambda.bridge.util.RequestUtils;
import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;
import com.digitalsanctum.lambda.model.DeleteContainerRequest;
import com.digitalsanctum.lambda.model.DeleteImageResponse;
import com.digitalsanctum.lambda.model.ListImagesResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Endpoints supporting Docker images.
 *
 * @author Shane Witbeck
 * @since 8/8/16
 */
public class ImageServlet extends HttpServlet {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final ImageService imageService;

  public ImageServlet(ImageService imageService) {
    this.imageService = imageService;
  }


  /**
   * List images.
   *
   * @param req
   * @param resp
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    ListImagesResponse listImagesResponse = imageService.listImages();
    String resultJson = mapper.writeValueAsString(listImagesResponse);

    resp.setStatus(listImagesResponse.getStatusCode());
    resp.getWriter().append(resultJson);
  }

  /**
   * Creates an image
   *
   * @param req
   * @param resp
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String json = RequestUtils.readRequestJson(req);
    CreateImageRequest createImageRequest = mapper.readValue(json, CreateImageRequest.class);

    // create the image
    CreateImageResponse result = imageService.createImage(createImageRequest);
    String resultJson = mapper.writeValueAsString(result);

    // write response
    resp.setStatus(HttpStatus.SC_CREATED);
    resp.getWriter().append(resultJson);
  }

  /**
   * Delete an image
   * <p>
   * DELETE /images/699655ad3beedd6d614694d8f2c4754398888f25faf62b6a021c9089c18b275b
   *
   * @param req
   * @param resp
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    String pathInfo = req.getPathInfo(); // /699655ad3beedd6d614694d8f2c4754398888f25faf62b6a021c9089c18b275b

    String id = pathInfo.substring(1); // 699655ad3beedd6d614694d8f2c4754398888f25faf62b6a021c9089c18b275b    

    DeleteContainerRequest deleteContainerRequest = new DeleteContainerRequest(id);

    DeleteImageResponse deleteImageResponse = imageService.deleteImage(id);
    String resultJson = mapper.writeValueAsString(deleteImageResponse);

    resp.setStatus(deleteImageResponse.getStatusCode());
    resp.getWriter().append(resultJson);
  }
}

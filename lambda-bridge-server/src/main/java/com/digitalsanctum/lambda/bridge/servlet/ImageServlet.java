package com.digitalsanctum.lambda.bridge.servlet;

import com.digitalsanctum.lambda.bridge.service.ImageService;
import com.digitalsanctum.lambda.bridge.util.RequestUtils;
import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.messages.Image;
import org.apache.http.HttpStatus;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Endpoints supporting Docker images.
 * 
 * @author Shane Witbeck
 * @since 8/8/16
 */
public class ImageServlet extends HttpServlet {

  private static final ObjectMapper mapper = new ObjectMapper();
  private final ImageService imageBuilder;

  public ImageServlet(ImageService imageBuilder) {
    this.imageBuilder = imageBuilder;
  }


  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    List<Image> images = imageBuilder.getFunctionImages();
    mapper.writerFor(new TypeReference<List<Image>>() {
    }).writeValue(resp.getWriter(), images);
  }

  /**
   * Creates a docker image
   *
   * @param req
   * @param resp
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String json = RequestUtils.readRequestJson(req);
    CreateImageRequest createImageRequest = mapper.readValue(json, CreateImageRequest.class);

    // create the image
    CreateImageResponse result = imageBuilder.createImage(createImageRequest);
    String resultJson = mapper.writeValueAsString(result);

    // write response
    resp.setStatus(HttpStatus.SC_CREATED);
    resp.getWriter().append(resultJson);
  }
}

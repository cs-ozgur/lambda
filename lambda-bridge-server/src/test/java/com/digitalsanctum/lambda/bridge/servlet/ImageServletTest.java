package com.digitalsanctum.lambda.bridge.servlet;

import com.digitalsanctum.lambda.bridge.server.DockerBridgeServer;
import com.digitalsanctum.lambda.bridge.service.DockerImageBuilderTest;
import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.digitalsanctum.lambda.model.CreateImageResponse;
import com.digitalsanctum.lambda.model.DeleteImageResponse;
import com.digitalsanctum.lambda.model.GetImageResponse;
import com.digitalsanctum.lambda.model.ListImagesResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.Charsets;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.digitalsanctum.lambda.model.HttpStatus.SC_CREATED;
import static com.digitalsanctum.lambda.model.HttpStatus.SC_NOT_FOUND;
import static com.digitalsanctum.lambda.model.HttpStatus.SC_OK;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class ImageServletTest {
  
  private static final Logger log = LoggerFactory.getLogger(ImageServletTest.class);
  
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String LAMBDA_JAR = "lambda.jar";
  private static final String APPLICATION_JSON = "application/json";
  private static final int TEST_PORT = 9000;
  private static final String BASE_URL = "http://localhost:" + TEST_PORT + "/images";
  
  private DockerBridgeServer dockerBridgeServer;
  private String imageName;
  
  @Before
  public void setup() throws Exception {
    imageName = "test1";
    dockerBridgeServer = new DockerBridgeServer(TEST_PORT);
    dockerBridgeServer.start();
  }
  
  @After 
  public void teardown() throws Exception {    
    dockerBridgeServer.stop();
  }
  
  @Test
  public void testCrud() throws Exception {
    
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      
      // list and verify image doesn't already exist
      ListImagesResponse listImagesResponse = doList(client);
      assertThat(listImagesResponse.getStatusCode(), is(SC_OK));
      
      // get, verify 404
      GetImageResponse getImageResponse = doGet(client);
      assertThat(getImageResponse.getStatusCode(), is(SC_NOT_FOUND));
      
      // create image
      CreateImageResponse createImageResponse = doCreate(client);
      assertThat(createImageResponse.getStatusCode(), is(SC_CREATED));
      
      // get, verify 200
      GetImageResponse getImageResponse2 = doGet(client);
      assertThat(getImageResponse2.getStatusCode(), is(SC_OK));      
      
      // delete
      DeleteImageResponse deleteImageResponse = doDelete(client);
      assertThat(deleteImageResponse.getStatusCode(), is(SC_OK));

      // get, verify 404
      GetImageResponse getImageResponse3 = doGet(client);
      assertThat(getImageResponse3.getStatusCode(), is(SC_NOT_FOUND));
    }
  }
  
  private DeleteImageResponse doDelete(CloseableHttpClient client) throws IOException {
    log.info("deleting {} image", imageName);
    HttpDelete delete = new HttpDelete(BASE_URL + "/" + imageName);
    CloseableHttpResponse response = client.execute(delete);
    HttpEntity entity = response.getEntity();
    String responseJson = EntityUtils.toString(entity, Charsets.UTF_8);
    return mapper.readValue(responseJson.getBytes(), DeleteImageResponse.class);
  }
  
  private GetImageResponse doGet(CloseableHttpClient client) throws IOException {
    log.info("getting {} image", imageName);
    HttpGet get = new HttpGet(BASE_URL + "/" + imageName);
    CloseableHttpResponse response = client.execute(get);
    HttpEntity entity = response.getEntity();
    String responseJson = EntityUtils.toString(entity, Charsets.UTF_8);
    return mapper.readValue(responseJson.getBytes(), GetImageResponse.class);
  }
  
  private ListImagesResponse doList(CloseableHttpClient client) throws IOException {
    log.info("listing images");
    HttpGet get = new HttpGet(BASE_URL);
    CloseableHttpResponse response = client.execute(get);
    HttpEntity entity = response.getEntity();
    String responseJson = EntityUtils.toString(entity, Charsets.UTF_8);
    return mapper.readValue(responseJson.getBytes(), ListImagesResponse.class);
  }
  
  private CreateImageResponse doCreate(CloseableHttpClient client) throws IOException {
    log.info("creating image: {}", imageName);
    
    HttpPost post = new HttpPost(BASE_URL);
    CreateImageRequest createImageRequest = new CreateImageRequest(imageName);

    InputStream is = DockerImageBuilderTest.class.getClassLoader().getResourceAsStream(LAMBDA_JAR);
    byte[] bytes = IOUtils.toByteArray(is);
    ByteBuffer lambdaByteBuffer = ByteBuffer.wrap(bytes);
    createImageRequest.setLambdaJar(lambdaByteBuffer);

    String requestJson = mapper.writeValueAsString(createImageRequest);
    StringEntity input = new StringEntity(requestJson);
    input.setContentType(APPLICATION_JSON);
    post.setEntity(input);

    CloseableHttpResponse response = client.execute(post);
    HttpEntity entity = response.getEntity();
    String responseJson = EntityUtils.toString(entity, Charsets.UTF_8);
    return mapper.readValue(responseJson.getBytes(), CreateImageResponse.class);
  }
}

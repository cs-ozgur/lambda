package com.digitalsanctum.lambda.bridge.servlet;

import com.digitalsanctum.lambda.bridge.server.DockerBridgeServer;
import com.digitalsanctum.lambda.bridge.service.DockerImageBuilderTest;
import com.digitalsanctum.lambda.model.CreateImageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author Shane Witbeck
 * @since 8/9/16
 */
public class BuilderServletTest {
  
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final int TEST_PORT = 9000;
  
  private DockerBridgeServer dockerBridgeServer;
  
  @Before
  public void setup() throws Exception {
    dockerBridgeServer = new DockerBridgeServer(TEST_PORT);
    dockerBridgeServer.start();
  }
  
  @After 
  public void teardown() throws Exception {
    dockerBridgeServer.stop();
  }
  
  @Test
  public void testCreate() throws Exception {
    System.out.println("todo");
    
    // TODO create a http client and test endpoint

    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpPost post = new HttpPost("http://localhost:" + TEST_PORT + "/images");

    CreateImageRequest request = new CreateImageRequest();
    request.setImageName("test1");

    // read test jar into ByteBuffer
    InputStream is = DockerImageBuilderTest.class.getClassLoader().getResourceAsStream("lambda.jar");
    byte[] bytes = IOUtils.toByteArray(is);
    ByteBuffer lambdaByteBuffer = ByteBuffer.wrap(bytes);
    request.setLambdaJar(lambdaByteBuffer);
    
    String requestJson = mapper.writeValueAsString(request);
    
    StringEntity input = new StringEntity(requestJson);
    post.setEntity(input);

    input.setContentType("application/json");
    
    CloseableHttpResponse response = httpclient.execute(post);
    System.out.println(response);
    Assert.assertEquals(201, response.getStatusLine().getStatusCode());
  }
}

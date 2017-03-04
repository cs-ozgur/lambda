package com.digitalsanctum.lambda.server.service;

import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.digitalsanctum.lambda.server.util.RESTUtils.isEndpointAvailable;

/**
 * @author Shane Witbeck
 * @since 3/3/17
 */
public class ContainerService {
  
  private static final Logger log = LoggerFactory.getLogger(ContainerService.class);

  private static final ObjectMapper mapper = new ObjectMapper();
  
  private static final Map<String, String> HANDLER_CONTAINER_ENDPOINT_CACHE = new ConcurrentHashMap<>();
  
  public String runContainer(CloseableHttpClient httpClient, 
                             String handler, 
                             String location) {

    // TODO verify a container already exists before creating a new one for each invocation
    
    String endpoint = null;
    
    if (HANDLER_CONTAINER_ENDPOINT_CACHE.containsKey(handler)) {
      endpoint = HANDLER_CONTAINER_ENDPOINT_CACHE.get(handler);
      log.info("found cached endpoint {} for handler {}", endpoint, handler);
    } else {
      
      RunContainerRequest runContainerRequest = new RunContainerRequest();
      runContainerRequest.setImageId(location);
      runContainerRequest.setHandler(handler);

      Optional<RunContainerResult> runContainerResult;
      try {

        // TODO make endpoint configurable
        HttpPost post = new HttpPost("http://localhost:8082/containers");

        String requestJson = mapper.writeValueAsString(runContainerRequest);
        StringEntity input = new StringEntity(requestJson);
        post.setEntity(input);
        input.setContentType("application/json");

        CloseableHttpResponse response = httpClient.execute(post);

        HttpEntity entity = response.getEntity();
        String responseJson = EntityUtils.toString(entity, Charsets.UTF_8);
        runContainerResult = Optional.ofNullable(mapper.readValue(responseJson.getBytes(), RunContainerResult.class));

        // cache the endpoint
        if (runContainerResult.isPresent()) {
          endpoint = runContainerResult.get().getEndpoint();
          HANDLER_CONTAINER_ENDPOINT_CACHE.put(handler, endpoint);
        }

        boolean up = false;
        log.info("waiting for container to become available");
        long start = System.currentTimeMillis();
        while (!up) {
          up = isEndpointAvailable(httpClient, endpoint, 500);
        }
        log.info("Container is available; took {} ms", (System.currentTimeMillis() - start));

      } catch (Exception e) {
        log.error("Error running container", e);
      }
    }
    
    return endpoint;
  }
  
}

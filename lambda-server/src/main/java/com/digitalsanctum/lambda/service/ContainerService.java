package com.digitalsanctum.lambda.service;

import com.digitalsanctum.lambda.model.ListContainersResponse;
import com.digitalsanctum.lambda.model.RunContainerRequest;
import com.digitalsanctum.lambda.model.RunContainerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.digitalsanctum.lambda.Configuration.CONTAINER_AVAILABILITY_SLEEP_PERIOD;
import static com.digitalsanctum.lambda.Configuration.CONTAINER_AVAILABILITY_TIMEOUT;
import static com.digitalsanctum.lambda.model.HttpStatus.SC_OK;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.codec.Charsets.UTF_8;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * @author Shane Witbeck
 * @since 3/3/17
 */
public class ContainerService {

  private static final Logger log = LoggerFactory.getLogger(ContainerService.class);

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final Map<String, String> FUNCTION_ENDPOINT_CACHE = new ConcurrentHashMap<>();

  public String runContainer(CloseableHttpClient httpClient,
                             final String bridgeServerEndpoint,
                             final String handler,
                             final String location,
                             final String functionName,
                             final Map<String, String> environmentVariables,
                             final Integer timeout) {

    String key = handler + location + functionName;
    if (FUNCTION_ENDPOINT_CACHE.containsKey(key)) {
      String functionEndpoint = FUNCTION_ENDPOINT_CACHE.get(key);
      log.info("found cached endpoint {} for handler {}", functionEndpoint, handler);

      // verify a container already exists before creating a new one for each invocation
      if (isContainerAvailable(httpClient, functionEndpoint)) {
        return functionEndpoint;
      }
    }

    // not found in cache or not available, create and run container
    Optional<RunContainerResponse> runContainerResponse;
    try {
      RunContainerRequest runContainerRequest = new RunContainerRequest(location, handler, functionName, environmentVariables, timeout);
      String runContainerRequestJson = mapper.writeValueAsString(runContainerRequest);
      StringEntity runContainerRequestEntity = new StringEntity(runContainerRequestJson);
      runContainerRequestEntity.setContentType(APPLICATION_JSON.toString());

      HttpPost post = new HttpPost(bridgeServerEndpoint + "/containers");
      post.setEntity(runContainerRequestEntity);

      CloseableHttpResponse response = httpClient.execute(post);

      HttpEntity entity = response.getEntity();
      String responseJson = EntityUtils.toString(entity, UTF_8);
      runContainerResponse = Optional.ofNullable(mapper.readValue(responseJson.getBytes(), RunContainerResponse.class));

      // cache the endpoint
      String functionEndpoint = null;
      if (runContainerResponse.isPresent()) {

        if (runContainerResponse.get().getErrorMessage() != null) {
          if (runContainerResponse.get().getStatusCode() == 409) {
            HttpGet listContainersGet = new HttpGet(bridgeServerEndpoint + "/containers");
            CloseableHttpResponse listResponse = httpClient.execute(listContainersGet);
            HttpEntity listEntity = listResponse.getEntity();
            String listResponseJson = EntityUtils.toString(listEntity, UTF_8);
            ListContainersResponse listContainersResponse = mapper.readValue(listResponseJson.getBytes(), ListContainersResponse.class);
            System.out.println(listContainersResponse);
          }
          throw new RuntimeException(runContainerResponse.get().getErrorMessage());
        }

        functionEndpoint = runContainerResponse.get().getEndpoint();
        blockUntilContainerAvailable(httpClient, functionEndpoint);
        FUNCTION_ENDPOINT_CACHE.put(key, functionEndpoint);
      }

      return functionEndpoint;

    } catch (Exception e) {
      log.error("Error running container", e);
    }
    return null;
  }

  private void blockUntilContainerAvailable(CloseableHttpClient httpClient, String endpoint) {
    boolean up = false;
    log.info("waiting for container to become available");
    long start = currentTimeMillis();
    while (!up) {
      up = isContainerAvailable(httpClient, endpoint);
      if (currentTimeMillis() - start >= CONTAINER_AVAILABILITY_TIMEOUT) {
        throw new RuntimeException("Timed out waiting for container to become available");
      }
    }
    log.info("Container is available at {}; took {} ms", endpoint, (currentTimeMillis() - start));
  }

  private static boolean isContainerAvailable(CloseableHttpClient httpClient, String endpoint) {
    try {
      HttpGet healthCheck = new HttpGet(endpoint);
      CloseableHttpResponse response = httpClient.execute(healthCheck);
      if (response.getStatusLine().getStatusCode() == SC_OK) {
        return true;
      } else {
        log.warn(response.toString());
      }
    } catch (Exception e) {
      try {
        Thread.sleep(CONTAINER_AVAILABILITY_SLEEP_PERIOD);
      } catch (InterruptedException e1) {
        // noop
      }
    }
    return false;
  }

}

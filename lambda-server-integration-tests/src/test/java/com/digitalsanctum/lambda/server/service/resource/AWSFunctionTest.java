package com.digitalsanctum.lambda.server.service.resource;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Shane Witbeck
 * @since 8/5/16
 */
public class AWSFunctionTest extends BaseTest {
  
  @Before
  public void setup() throws Exception {
    AWSCredentialsProvider awsCredentialsProvider = new ProfileCredentialsProvider("shane");
    awsLambda = new AWSLambdaClient(awsCredentialsProvider);
    awsLambda.setRegion(Region.getRegion(Regions.US_WEST_2));
  }
  
  @Test
  public void testInvokeRequest_RequestResponse() throws Exception {
    invoke_RequestResponse();
  }
}

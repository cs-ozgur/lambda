package com.digitalsanctum.dynamodb;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Shane Witbeck
 * @since 8/16/16
 */
public class DockerDynamoDBTest {

  private static final String TEST_TABLE = "test";
  private static final String TEST_KEY = "id";

  private static AmazonDynamoDB amazonDynamoDB;
  private static DockerDynamoDB dockerDynamoDB;

  @BeforeClass
  public static void setupClazz() throws Exception {
    dockerDynamoDB = new DockerDynamoDB();
    int port = dockerDynamoDB.start();

    amazonDynamoDB = AmazonDynamoDBClient.builder()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:" + port, "local"))
        .build();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    dockerDynamoDB.stop();
  }

  @Test
  public void testCreateTable() throws Exception {

    List<KeySchemaElement> schemaElements = new ArrayList<>();
    KeySchemaElement element = new KeySchemaElement(TEST_KEY, KeyType.HASH);
    schemaElements.add(element);

    amazonDynamoDB.createTable(new CreateTableRequest(TEST_TABLE, schemaElements)
        .withAttributeDefinitions(new AttributeDefinition(TEST_KEY, ScalarAttributeType.S))
        .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L)));

    DescribeTableResult describeTableResult = amazonDynamoDB.describeTable(TEST_TABLE);
    String actualKeyName = describeTableResult.getTable().getKeySchema().get(0).getAttributeName();

    assertThat(actualKeyName, is(TEST_KEY));
  }
}

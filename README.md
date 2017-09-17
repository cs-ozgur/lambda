
# Lambda

Develop AWS Lambda functions locally along with supporting AWS infrastructure.

**NOTE: This project continues to be a work in progress. Expect breaking changes.**


## Prerequistes

- Java 8
- Maven
- Docker engine

## Build

    ./mvnw clean install
    
## Features
- Deploy Lambda functions locally and invoke them
- Aims for 100% compatibility with official AWS Lambda APIs
- Streams support (in progress):
    - DynamoDB Streams (via containerized DynamoDB Local)
    - Kinesis Streams (via containerized kinesalite) 
- Utilizes Docker for:
    - packaging and running Lambda functions
    - wrap supporting AWS infrastructure (DynamoDB Local, Kinesalite, etc.)
- Implements AWS Lambda HTTP endpoints
- S3 support (via containerized fake-s3)
- SQS support (via containerized elasticmq)
- Elasticache Redis support (via containerized redis)

## Road map

- API Gateway
- CloudFormation
- Cloudwatch
- Elasticsearch (via containerized elasticsearch)
- More examples
- ?

## Supported AWS Endpoints

| AWS Service | Supported Endpoints|
|---|---|
| Lambda | CreateFunction, DeleteFunction, GetFunction, Invoke, ListFunctions, CreateEventSourceMapping, ListEventSourceMappings, UpdateEventSourceMapping |
| DynamoDB | All endpoints supported by DynamoDB Local |
| Kinesis Streams | All endpoints supported by Kinesalite |


You can use the AWS CLI and AWS SDK to operate like you normally would with real AWS endpoints.

## Experimental Lifecycle DSL

The following will start the Lambda server and supporting infrastructure:

    AWSLocal awsLocal = AWSLocal.builder(LambdaServiceType.FILESYSTEM)
            .enableDynamoDB()
            .enableElasticacheRedis()
            .enableKinesisStreams()
            .enableS3()
            .enableSQS()
            .build()
            .start();

    // instantiate DynamoDB client to point to local DynamoDB
    String dynamoDbEndpoint = awsLocal.getDynamoDbEndpoint();
    EndpointConfiguration dynamoDbEndpointConfiguration = new EndpointConfiguration(dynamoDbEndpoint, "local");
    amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
        .withEndpointConfiguration(dynamoDbEndpointConfiguration)
        .build();

    // instantiate Lambda client to point to local Lambda API server
    AwsClientBuilder.EndpointConfiguration endpointConfiguration
        = new AwsClientBuilder.EndpointConfiguration("http://localhost:8080", awsLocal.getSigningRegion());
    awsLambda = AWSLambdaClientBuilder.standard().withEndpointConfiguration(endpointConfiguration).build();

    // do some work

    awsLocal.stop();


By default, AWS Lambda API endpoints are available at `http://localhost:8080`


## Usage

From the command line:

    java -jar lambda-lifecycle/target/lambda-lifecycle-1.7.jar
    
From Java code:
    
    AWSLocal awsLocal = AWSLocal.builder(AWSLocal.LambdaServiceType.FILESYSTEM)
        .enableDynamoDB()
        .enableKinesisStreams()
        .build()
        .start();    

### AWS CLI
    
List functions:
    
    aws lambda list-functions --profile local --endpoint-url http://localhost:8080
       
Create a function:       
       
    cd <LAMBDA_PROJECT_ROOT>       
    aws lambda create-function --function-name test1 \
       --runtime java8 \
       --role arn:aws:iam::111111111111:role/lambda_basic_execution \
       --handler com.digitalsanctum.lambda.functions.requestresponse.Concat \
       --zip-file fileb://lambda-server-integration-tests/src/test/resources/test-functions/lambda.jar \
       --description "test1 description" \
       --timeout 30 \
       --memory-size 512 \
       --endpoint-url http://localhost:8080
                  
Get a function:       
    
    aws lambda get-function --function-name test1 --endpoint-url http://localhost:8080    
    
Invoke a function:    
    
    aws lambda invoke --function-name test1 --payload "{\"firstName\":\"shane\",\"lastName\":\"witbeck\"}" --endpoint-url http://localhost:8080 output.json    
                
Update code for a function (update `com.digitalsanctum.lambda.functions.requestresponse.Concat`, cd lambda-functions):

Make a change to `com.digitalsanctum.lambda.functions.requestresponse.Concat` then do the following steps to see your
change applied to the Lambda function:

    ./mvnw install -pl lambda-functions -am
            
    
    aws lambda update-function-code --function-name test1 \
        --zip-file fileb://lambda-server-integration-tests/src/test/resources/test-functions/lambda.jar \
        --endpoint-url http://localhost:8080
                    
    aws lambda invoke --function-name test1 --payload "{\"firstName\":\"shane\",\"lastName\":\"witbeck\"}" --endpoint-url http://localhost:8080 output2.json                    
    
Delete a function:

    aws lambda delete-function --function-name test1 --profile local --endpoint-url http://localhost:8080
    
Confirm the deletion by listing functions again:

    aws lambda list-functions --profile local --endpoint-url http://localhost:8080   
    
    
List event source mappings:
    
    aws lambda list-event-source-mappings --endpoint-url http://localhost:8080
    
    
Create an event source mapping:
                          
    aws lambda create-event-source-mapping --event-source-arn arn:aws:kinesis:local:111111111111:stream/foo \
        --function-name test1 \
        --starting-position TRIM_HORIZON \
        --endpoint-url http://localhost:8080
    
    
### AWS Java SDK
    
Instantiating an AWS Lambda client to work with local functions:    
    
    AwsClientBuilder.EndpointConfiguration endpointConfiguration 
            = new AwsClientBuilder.EndpointConfiguration("http://localhost:8080", "local");
            
    AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
        .withEndpointConfiguration(endpointConfiguration)
        .build();
    
        
        

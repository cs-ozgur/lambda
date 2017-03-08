
# Lambda

Deploy and invoke AWS Lambda functions locally.

**NOTE: This project continues to be a work in progress. Expect breaking changes.**


![Component Diagram](etc/images/component_diagram.png?raw=true "Component Diagram")


## Prerequistes

- Java 8
- Maven
- Docker engine

## build

    ./mvnw clean install
    
## Features
- Deploy Lambda functions locally and invoke them
- Compatible with official AWS Lambda APIs
- Partial support for other AWS resources (see Road map below)
    - DynamoDB (via containerized DynamoDB Local)
    - Kinesis Streams (via containerized kinesalite) 
- lambda-bridge-server: manages underlying Docker images and containers to support Lambda functions
- lambda-server: hosts Lambda endpoints; delegates to lambda-bridge-server for CreateFunction and Invoke Lambda actions. 


## Road map

- Support for event sources such as AWS DynamoDB and AWS Kinesis Streams (in progress)
- S3 (via containerized fake-S3)
- SQS (via containerized elasticmq)
- API Gateway
- CloudFormation
- Cloudwatch
- EC2 (via AMI -> container conversion?)
- Elasticache (via containerized redis)
- Elasticsearch (via containerized elasticsearch)
- More examples
- ?


## lambda-bridge-server

Serves as a proxy between lambda-server and Docker engine. Responsible for building Docker images that wrap Lambda functions 
and creating and running Docker containers.

## lambda-server

A standalone server that implements the following AWS Lambda endpoints:

- CreateFunction
- DeleteFunction
- GetFunction
- Invoke
- ListFunctions

- CreateEventSourceMapping
- ListEventSourceMappings
- UpdateEventSourceMapping


You can use the AWS CLI and AWS Java SDK to operate like you normally would with real AWS endpoints.

## Usage

Start lambda-bridge-server:

    cd lambda-bridge-server/target    
    java -jar lambda-bridge-server-1.0-SNAPSHOT.jar

In a separate terminal window, start lambda-server:

    cd lambda-server/target    
    java -jar lambda-server-1.0-SNAPSHOT.jar 
   
will start the server on port 8080 by default. Or provide the port to run on:
    
    java -jar lambda-server-1.0-SNAPSHOT.jar 7000

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
                
Update code for a function:

    aws lambda update-function-code --function-name test1 \
        --zip-file fileb://lambda-server-integration-tests/src/test/resources/test-functions/lambda2.jar \
        --endpoint-url http://localhost:8080            
    
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
    
    AWSLambda awsLambda = AwsClientBuilder.EndpointConfiguration endpointConfiguration 
            = new AwsClientBuilder.EndpointConfiguration("http://localhost:8080", "local");
            
    awsLambda = AWSLambdaClientBuilder.standard()
        .withEndpointConfiguration(endpointConfiguration)
        .build();
    
        
        

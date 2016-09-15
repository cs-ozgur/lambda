
# Lambda

Deploy and invoke AWS Lambda functions locally.

**NOTE: This project continues to be a work in progress. Expect breaking changes.**


![Component Diagram](etc/images/component_diagram.png?raw=true "Component Diagram")


## Prerequistes

- Java 8
- Maven
- Docker engine
- etcd

## build

    ./mvnw clean install
    
## Features
- Deploy Lambda functions locally and invoke them
- Compatible with AWS Lambda SDK and CLI
- lambda-bridge-server: manages underlying Docker images and containers to support Lambda functions
- lambda-server: hosts Lambda endpoints; delegates to lambda-bridge-server for CreateFunction and Invoke Lambda actions. 

## Road map

- Persist configurations between server restarts.
- Support for event sources such as AWS DynamoDB and AWS Kinesis Streams (in progress)
- More examples
- ?

## etcd

etcd is used for service discovery. Start it with the following:

    docker run \
      -d \
      -p 2379:2379 \
      -p 2380:2380 \
      -p 4001:4001 \
      -p 7001:7001 \
      -v /path/to/data/dir:/data \
      --name etcd0 \
      elcolio/etcd:latest \
      -name etcd0

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

You can use the AWS CLI and AWS Java SDK to operate like you normally would with real AWS endpoints.

## Usage

Start lambda-bridge-server:

    cd lambda-bridge-server/target    
    java -jar lambda-bridge-server-1.0-SNAPSHOT.jar

Start lambda-server:

    cd lambda-servet/target    
    java -jar lambda-server-1.0-SNAPSHOT.jar 
   
will start the server on port 8080 by default. Or provide the port to run on:
    
    java -jar lambda-server-1.0-SNAPSHOT.jar 7000

### AWS CLI
    
List functions (Local):
    
    aws lambda list-functions --profile local --endpoint-url http://localhost:8080
       
Create a function (Local):       
       
    aws lambda create-function --function-name test1 \
       --runtime java8 \
       --role arn:aws:iam::515292396565:role/lambda_basic_execution \
       --handler com.digitalsanctum.lambda.functions.requestresponse.Concat \
       --zip-file fileb://lambda-server-integration-tests/src/test/resources/test-functions/lambda.jar \
       --description "test1 description" \
       --timeout 30 \
       --memory-size 512 \
       --endpoint-url http://localhost:8080
                  
Get a function (Local):       
    
    aws lambda get-function --function-name test1 --endpoint-url http://localhost:8080    
    
Invoke a function (Local):    
    
    aws lambda invoke --function-name test1 --payload "{\"firstName\":\"shane\",\"lastName\":\"witbeck\"}" --endpoint-url http://localhost:8080 output.json    
                
Update code for a function (Local):

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
    
    new AWSLambdaClient()
        .withEndpoint("http://localhost:8080)
        .listFunctions(new ListFunctionsRequest().withMaxItems(10));
        
        

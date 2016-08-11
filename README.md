
# Lambda

Deploy and invoke AWS Lambda functions locally.

**NOTE: This project continues to be a work in progress. Expect breaking changes.**


## Prerequistes

- Java 8
- Maven
- Docker engine

## build

    mvn clean install
    
## Features
- Deploy Lambda functions locally and invoke them
- Compatible with AWS Lambda SDK and CLI
- lambda-bridge-server  
- lambda-server

## Road map

- Support for event sources such as AWS DynamoDB and AWS Kinesis Streams


## lambda-docker-bridge

Serves as a proxy between lambda-server and Docker engine.

## lambda-server

A standalone server that implements the following AWS Lambda endpoints:

- CreateFunction
- DeleteFunction
- GetFunction
- Invoke
- ListFunctions

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

AWS CLI:

List functions (AWS):

    aws lambda list-functions --profile shane
    
List functions (Local):
    
    aws lambda list-functions --profile local --endpoint-url http://localhost:8080
    
Create a function (AWS):    
    
    aws lambda create-function --function-name test1 \
       --runtime java8 \
       --role arn:aws:iam::515292396565:role/lambda_basic_execution \
       --handler com.digitalsanctum.lambda.samples.HelloPojo \
       --zip-file fileb://lambda-server-integration-tests/src/test/resources/test-functions/lambda.jar \
       --description "test1 description" \
       --timeout 30 \
       --memory-size 512 \
       --profile shane
       
Create a function (Local):       
       
    aws lambda create-function --function-name test1 \
       --runtime java8 \
       --role arn:aws:iam::515292396565:role/lambda_basic_execution \
       --handler com.digitalsanctum.lambda.samples.HelloPojo \
       --zip-file fileb://lambda-server-integration-tests/src/test/resources/test-functions/lambda.jar \
       --description "test1 description" \
       --timeout 30 \
       --memory-size 512 \
       --endpoint-url http://localhost:8080
                  
       
Get a function (AWS):       
    
    aws lambda get-function --function-name test1 --profile shane
    
Get a function (Local):       
    
    aws lambda get-function --function-name test1 --endpoint-url http://localhost:8080    
    
Invoke a function (AWS):    
    
    aws lambda invoke --function-name test1 --payload "{\"firstName\":\"shane\",\"lastName\":\"witbeck\"}" --profile shane output.json
    
Invoke a function (Local):    
    
    aws lambda invoke --function-name test1 --payload "{\"firstName\":\"shane\",\"lastName\":\"witbeck\"}" --endpoint-url http://localhost:8080 output.json    
    
Update code for a function (AWS):

    aws lambda update-function-code --function-name test1 \
        --zip-file fileb://lambda-server-integration-tests/src/test/resources/test-functions/lambda2.jar \
        --profile shane
                
Update code for a function (Local):

    aws lambda update-function-code --function-name test1 \
        --zip-file fileb://lambda-server-integration-tests/src/test/resources/test-functions/lambda2.jar \
        --endpoint-url http://localhost:8080            
    
Delete a function:

    aws lambda delete-function --function-name test1 --profile local --endpoint-url http://localhost:8080
    
Confirm the deletion by listing functions again:

    aws lambda list-functions --profile local --endpoint-url http://localhost:8080   
    
AWS Java SDK:
    
    new AWSLambdaClient()
        .withEndpoint("http://localhost:8080)
        .listFunctions(new ListFunctionsRequest().withMaxItems(10));
        
        

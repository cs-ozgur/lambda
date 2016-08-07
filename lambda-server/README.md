
# lambda-server

A standalone server that implements most of the AWS Lambda endpoints:

- CreateFunction
- DeleteFunction
- GetFunction
- Invoke
- ListFunctions

You can use the AWS CLI and AWS Java SDK to operate like you normally would with real AWS endpoints.

## prerequistes

- Java 8
- Maven

## build

    mvn clean install
    
Will produce a fat jar: `target/lambda-server-VERSION.jar`    

## usage

Start lambda-server:
    
    java -jar lambda-server-1.0-SNAPSHOT.jar 
   
will start the server on port 8080 by default. Or provide the port to run on:
    
    java -jar lambda-server-1.0-SNAPSHOT.jar 7000

AWS CLI:

List functions:

    aws lambda list-functions --profile local --endpoint-url http://localhost:8080
    aws lambda list-functions --profile shane
    
Create a function (AWS):    
    
    aws lambda create-function --function-name test1 \
       --runtime java8 \
       --role arn:aws:iam::515292396565:role/lambda_basic_execution \
       --handler com.digitalsanctum.lambda.samples.HelloPojo \
       --zip-file fileb:///Users/switbe/projects/lambda/lambda-server-integration-tests/src/test/resources/test-functions/lambda.jar \
       --description "test1 description" \
       --timeout 30 \
       --memory-size 512 \
       --profile shane
       
Create a function (Local):       
       
    aws lambda create-function --function-name test1 \
       --runtime java8 \
       --role arn:aws:iam::515292396565:role/lambda_basic_execution \
       --handler com.digitalsanctum.lambda.samples.HelloPojo \
       --zip-file fileb:///Users/switbe/projects/lambda/lambda-server-integration-tests/src/test/resources/test-functions/lambda.jar \
       --description "test1 description" \
       --timeout 30 \
       --memory-size 512 \
       --endpoint-url http://localhost:8080
                  
       
Get a function (AWS):       
    
    aws lambda get-function --function-name test1 --profile shane
    
Get a function (Local):       
    
    aws lambda get-function --function-name test1 --endpoint-url http://localhost:8080    
    
Invoke a function (AWS):    
    
    aws lambda invoke --function-name test1 --payload "{\"firstName\":\"shane\",\"lastName\":\"witbeck\"}" --profile shane foo.json
    
Invoke a function (Local):    
    
    aws lambda invoke --function-name test1 --payload "{\"firstName\":\"shane\",\"lastName\":\"witbeck\"}" --endpoint-url http://localhost:8080 foo.json    
    
Update code for a function (AWS):

    aws lambda update-function-code --function-name test1 \
        --zip-file fileb:///Users/switbe/projects/lambda/lambda-server-integration-tests/src/test/resources/test-functions/lambda2.jar \
        --profile shane
                
Update code for a function (Local):

    aws lambda update-function-code --function-name test1 \
        --zip-file fileb:///Users/switbe/projects/lambda/lambda-server-integration-tests/src/test/resources/test-functions/lambda2.jar \
        --endpoint-url http://localhost:8080            
    
Delete a function:

    aws lambda delete-function --function-name test1 --profile shane
    
Confirm the deletion by listing functions again:

    aws lambda list-functions --profile shane     
    
AWS Java SDK:
    
    new AWSLambdaClient()
        .withEndpoint("http://localhost:8080)
        .listFunctions(new ListFunctionsRequest().withMaxItems(10));
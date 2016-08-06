
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

    aws lambda list-functions --profile local --endpoint-url http://localhost:8080
    
AWS Java SDK:
    
    new AWSLambdaClient()
        .withEndpoint("http://localhost:8080)
        .listFunctions(new ListFunctionsRequest().withMaxItems(10));
FROM java
MAINTAINER Shane Witbeck <shane@digitalsanctum.com>

ADD ./target/lambda-proxy-1.9.jar /data/
ADD ./lambda.jar /data/
WORKDIR /data

EXPOSE 8081

CMD java -Xmx256m -cp /data/lambda.jar -jar /data/lambda-proxy-1.9.jar

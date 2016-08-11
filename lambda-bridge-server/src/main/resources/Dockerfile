FROM java
MAINTAINER Shane Witbeck <shane@digitalsanctum.com>

ADD ./proxy.jar /data/
ADD ./lambda.jar /data/
WORKDIR /data

EXPOSE 8080

CMD java -Xmx256m -cp 'lambda.jar:proxy.jar' com.digitalsanctum.lambda.proxy.App

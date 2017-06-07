FROM java
MAINTAINER Shane Witbeck <shane@digitalsanctum.com>

ADD ./proxy.jar /data/
ADD ./lambda.jar /data/
WORKDIR /data

EXPOSE 8080
EXPOSE 5005

CMD java -Xmx256m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -cp 'lambda.jar:proxy.jar' com.digitalsanctum.lambda.proxy.App

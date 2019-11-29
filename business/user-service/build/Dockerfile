FROM openjdk:8-jre-alpine

RUN \
 apk update && apk add ca-certificates wget && \
 update-ca-certificates && apk add maven && \
 wget "http://www.dclunie.com/pixelmed/software/20191022_current/pixelmed.jar" && \
 mvn install:install-file -Dfile=pixelmed.jar \
 -DgroupId=pixelmed -DartifactId=pixelmed -Dversion=1.0 -Dpackaging=jar

COPY business/user-service/target/user-service-1.0.jar target/

CMD ["java", "-jar", "target/user-service-1.0.jar"]
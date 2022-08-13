FROM openjdk:17-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /storage-service.jar
ENTRYPOINT ["java","-jar","/storage-service.jar"]
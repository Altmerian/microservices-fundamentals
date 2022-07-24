FROM openjdk:17-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /song-service.jar
ENTRYPOINT ["java","-jar","/song-service.jar"]
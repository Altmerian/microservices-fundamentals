FROM openjdk:17-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /resource-processor.jar
ENTRYPOINT ["java","-jar","/resource-processor.jar"]
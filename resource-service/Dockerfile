FROM openjdk:17-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /resource-service.jar
ENTRYPOINT ["java","-jar","/resource-service.jar"]
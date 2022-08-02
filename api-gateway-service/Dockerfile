FROM openjdk:17-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /api-gateway-service.jar
ENTRYPOINT ["java","-jar","/api-gateway-service.jar"]
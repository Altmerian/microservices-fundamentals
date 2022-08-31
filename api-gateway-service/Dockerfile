FROM openjdk:17-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /api-gateway-service.jar
COPY build/otel/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
ENTRYPOINT ["java","-jar","-javaagent:/opentelemetry-javaagent.jar","/api-gateway-service.jar"]
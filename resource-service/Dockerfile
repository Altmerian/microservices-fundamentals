FROM openjdk:17-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /resource-service.jar
COPY build/otel/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
ENTRYPOINT ["java","-jar","-javaagent:/opentelemetry-javaagent.jar","/resource-service.jar"]
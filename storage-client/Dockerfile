FROM openjdk:17-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /storage-client.jar
COPY build/otel/opentelemetry-javaagent.jar /opentelemetry-javaagent.jar
ENTRYPOINT ["java","-jar","-javaagent:/opentelemetry-javaagent.jar","/storage-client.jar"]
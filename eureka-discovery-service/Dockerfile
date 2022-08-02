FROM openjdk:17-jdk-alpine
ARG JAR_FILE
COPY ${JAR_FILE} /eureka-discovery-service.jar
ENTRYPOINT ["java","-jar","/eureka-discovery-service.jar"]
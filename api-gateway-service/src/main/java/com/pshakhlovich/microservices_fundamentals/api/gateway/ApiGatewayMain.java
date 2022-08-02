package com.pshakhlovich.microservices_fundamentals.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ApiGatewayMain {

  public static void main(String[] args) {
    SpringApplication.run(ApiGatewayMain.class, args);
  }
}

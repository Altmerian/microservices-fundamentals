package com.pshakhlovich.microservices_fundamentals.eureka.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaDiscoveryMain {

  public static void main(String[] args) {
    SpringApplication.run(EurekaDiscoveryMain.class, args);
  }
}

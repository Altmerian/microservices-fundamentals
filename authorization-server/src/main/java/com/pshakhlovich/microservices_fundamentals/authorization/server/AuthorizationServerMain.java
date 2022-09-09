package com.pshakhlovich.microservices_fundamentals.authorization.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthorizationServerMain {

  public static void main(String[] args) {
    SpringApplication.run(AuthorizationServerMain.class, args);
  }
}

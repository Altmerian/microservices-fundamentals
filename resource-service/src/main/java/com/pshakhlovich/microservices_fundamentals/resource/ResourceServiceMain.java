package com.pshakhlovich.microservices_fundamentals.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ResourceServiceMain {

  public static void main(String[] args) {
    SpringApplication.run(ResourceServiceMain.class, args);
  }
}

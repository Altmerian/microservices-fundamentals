package com.pshakhlovich.microservices_fundamentals.resource.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableDiscoveryClient
public class ResourceProcessorMain {

  public static void main(String[] args) {
    SpringApplication.run(ResourceProcessorMain.class, args);
  }
}

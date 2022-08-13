package com.pshakhlovich.microservices_fundamentals.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StorageServiceMain {

  public static void main(String[] args) {
    SpringApplication.run(StorageServiceMain.class, args);
  }
}

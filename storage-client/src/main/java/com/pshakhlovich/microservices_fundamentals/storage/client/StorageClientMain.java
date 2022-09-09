package com.pshakhlovich.microservices_fundamentals.storage.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StorageClientMain {

  public static void main(String[] args) {
    SpringApplication.run(StorageClientMain.class, args);
  }
}

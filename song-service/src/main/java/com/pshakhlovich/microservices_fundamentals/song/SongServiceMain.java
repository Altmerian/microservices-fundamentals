package com.pshakhlovich.microservices_fundamentals.song;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SongServiceMain {

  public static void main(String[] args) {
    SpringApplication.run(SongServiceMain.class, args);
  }
}

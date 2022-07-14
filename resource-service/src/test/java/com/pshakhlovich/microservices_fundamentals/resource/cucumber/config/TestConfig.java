package com.pshakhlovich.microservices_fundamentals.resource.cucumber.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties
@ComponentScan("com.pshakhlovich.microservices_fundamentals.resource")
public class TestConfig {

  @Bean
  S3Client s3Client() {
    return S3Client.create();
  }
}

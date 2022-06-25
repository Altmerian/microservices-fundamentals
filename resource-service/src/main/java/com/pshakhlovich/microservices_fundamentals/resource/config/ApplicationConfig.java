package com.pshakhlovich.microservices_fundamentals.resource.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

  private final Mp3BucketProperties mp3BucketProperties;

  @Bean
  S3Client s3Client() {
    return S3Client.builder()
            .region(Region.of(mp3BucketProperties.getRegion()))
            .build();
  }
}

package com.pshakhlovich.microservices_fundamentals.resource.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "s3")
@Component
public class Mp3BucketProperties {

  private String bucketName;
  private String region;
}

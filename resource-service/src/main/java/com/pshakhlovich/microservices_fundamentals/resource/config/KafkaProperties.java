package com.pshakhlovich.microservices_fundamentals.resource.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
@Component
public class KafkaProperties {

  private String bootstrapAddress;
  private String resourceTopic;
}

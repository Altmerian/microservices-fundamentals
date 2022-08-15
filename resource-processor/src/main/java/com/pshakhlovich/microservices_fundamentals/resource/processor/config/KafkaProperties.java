package com.pshakhlovich.microservices_fundamentals.resource.processor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka")
@Component
public class KafkaProperties {

  private String bootstrapAddress;
  private String resourceTopic;
  private Map<String, String> properties;
}

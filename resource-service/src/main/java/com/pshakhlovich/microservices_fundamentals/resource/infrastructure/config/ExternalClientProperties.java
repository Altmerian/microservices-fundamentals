package com.pshakhlovich.microservices_fundamentals.resource.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "infrastructure")
@Component
public class ExternalClientProperties {

  private String storageServiceId;
}

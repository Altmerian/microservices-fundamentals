package com.pshakhlovich.microservices_fundamentals.authorization.server.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.security.oauth2")
@Component
public class OauthProperties {

  private String clientUri;
  private String issuerUri;
}

package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

@Component
@Slf4j
public class ResourceClientImpl implements ResourceClient {

  private static final Duration CONNECTION_TIMEOUT = Duration.of(30, SECONDS);
  private static final int BACKOFF_DELAY = 1000;
  private static final String RESOURCE_URL = "http://localhost:8081/resources/";

  private final RestTemplate restTemplate;

  @Autowired
  public ResourceClientImpl(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate =
        restTemplateBuilder
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setReadTimeout(CONNECTION_TIMEOUT)
            .build();
  }

  @Override
  @Retryable(
      value = ResponseStatusException.class,
      backoff = @Backoff(delay = BACKOFF_DELAY),
      listeners = "resourceClientRetryListener")
  public ByteArrayResource getResource(Integer resourceId) {
    try {
      return restTemplate.getForObject(RESOURCE_URL + resourceId, ByteArrayResource.class);
    } catch (RestClientException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get resource from 'resource-service'", e);
    }
  }
}

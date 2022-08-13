package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.resource;

import com.pshakhlovich.microservices_fundamentals.resource.processor.dto.ReUploadDto;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.config.ExternalClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
  private static final String RESOURCE_BASE_URL = "/resources/";

  private final RestTemplate restTemplate;
  private final ExternalClientProperties clientProperties;
  private final LoadBalancerClient loadBalancer;

  @Autowired
  public ResourceClientImpl(RestTemplateBuilder restTemplateBuilder, ExternalClientProperties clientProperties, LoadBalancerClient loadBalancer) {
    this.restTemplate =
        restTemplateBuilder
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setReadTimeout(CONNECTION_TIMEOUT)
            .build();
    this.clientProperties = clientProperties;
    this.loadBalancer = loadBalancer;
  }

  @Override
  @Retryable(
      value = ResponseStatusException.class,
      backoff = @Backoff(delay = BACKOFF_DELAY),
      listeners = "resourceClientRetryListener")
  public ByteArrayResource getResource(Integer resourceId) {
    try {
      ServiceInstance resourceServiceInstance = loadBalancer.choose(clientProperties.getResourceServiceId());
      var baseUrl = resourceServiceInstance.getUri().toString();

      return restTemplate.getForObject(baseUrl + RESOURCE_BASE_URL + resourceId, ByteArrayResource.class);
    } catch (RestClientException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get resource from 'resource-service'", e);
    }
  }

  @Override
  public void reUpload(ReUploadDto reUploadDto) {
    ServiceInstance resourceServiceInstance = loadBalancer.choose(clientProperties.getResourceServiceId());
    var baseUrl = resourceServiceInstance.getUri().toString();

    HttpEntity<ReUploadDto> entity = new HttpEntity<>(reUploadDto);
    restTemplate.exchange(baseUrl + RESOURCE_BASE_URL, HttpMethod.PUT, entity, Void.class);
  }
}

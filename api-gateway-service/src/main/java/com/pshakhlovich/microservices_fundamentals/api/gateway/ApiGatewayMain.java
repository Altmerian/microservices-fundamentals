package com.pshakhlovich.microservices_fundamentals.api.gateway;

import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@SpringBootApplication
@EnableEurekaClient
public class ApiGatewayMain {

  public static void main(String[] args) {
    SpringApplication.run(ApiGatewayMain.class, args);
  }

  @Bean
  public CommonsRequestLoggingFilter logFilter() {
    CommonsRequestLoggingFilter filter
            = new CommonsRequestLoggingFilter();
    filter.setIncludeQueryString(true);
    filter.setIncludeHeaders(true);
    filter.setIncludeClientInfo(true);
    return filter;
  }

  @Bean
  MeterRegistryCustomizer<MeterRegistry> registryCustomizer(
          @Value("${spring.application.name}") String applicationName) {
    return (registry) -> registry.config().commonTags("application", applicationName);
  }

  @Bean
  public MeterBinder processMemoryMetrics() {
    return new ProcessMemoryMetrics();
  }

  @Bean
  public MeterBinder processThreadMetrics() {
    return new ProcessThreadMetrics();
  }
}

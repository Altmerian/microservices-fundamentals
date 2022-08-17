package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.storage;

import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.StorageMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.config.ExternalClientProperties;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.exception.InfrastructureException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageClient {

    private static final String STORAGE_BASE_URL = "/storages";

    private final ExternalClientProperties clientProperties;

    private final LoadBalancerClient loadBalancer;

    private volatile List<StorageMetadata> cashedStoragesMetadata = Collections.emptyList();


    @CircuitBreaker(name = "storage-client", fallbackMethod = "storageClientFallback")
    @Retry(name = "storage-client")
    public List<StorageMetadata> getAllStoragesMetadata() throws InfrastructureException {
        try {
            ServiceInstance songServiceInstance = loadBalancer.choose(clientProperties.getStorageServiceId());
            var baseUrl = songServiceInstance.getUri().toString();

            ResponseEntity<List<StorageMetadata>> responseEntity =
                    new RestTemplate().exchange(
                            baseUrl + STORAGE_BASE_URL,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<>() {
                            }
                    );
            List<StorageMetadata> storagesMetadata = responseEntity.getBody();
            Objects.requireNonNull(storagesMetadata);

            if (!storagesMetadata.isEmpty()) {
                this.cashedStoragesMetadata = storagesMetadata;
                return storagesMetadata;
            } else {
                throw new RuntimeException("No storages' metadata were found.");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InfrastructureException(e.getMessage());
        }
    }

    private List<StorageMetadata> storageClientFallback(Exception e) {
        if (!cashedStoragesMetadata.isEmpty()) {
            log.info("'storage-client' circuit-breaker fallback is active. Getting cashed storage metadata.");
            return cashedStoragesMetadata;
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "'storage-client' circuit-breaker fallback method couldn't get cashed storage metadata.", e);
        }
    }
}

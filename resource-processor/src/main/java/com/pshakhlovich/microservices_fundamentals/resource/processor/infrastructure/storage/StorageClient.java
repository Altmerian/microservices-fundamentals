package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.storage;

import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.StorageMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.StorageMetadata.StorageType;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageClient {

    private static final String STORAGE_BASE_URL = "/storages";

    private final ExternalClientProperties clientProperties;

    private final LoadBalancerClient loadBalancer;

    private StorageMetadata cashedStorageMetadata;

    @CircuitBreaker(name = "storage-client", fallbackMethod = "storageClientFallback")
    @Retry(name = "storage-client")
    public StorageMetadata getPermanentStorage() {
        List<StorageMetadata> result = getAllStoragesMetadata();

        Optional<StorageMetadata> permanentStorageOptional = result.stream()
                .filter(storageMetadataDto -> storageMetadataDto.getStorageType().equals(StorageType.PERMANENT))
                .findAny();
        if (permanentStorageOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No permanent storages were found.");
        }
        cashedStorageMetadata = permanentStorageOptional.get();
        return permanentStorageOptional.get();
    }

    private List<StorageMetadata> getAllStoragesMetadata() throws InfrastructureException {
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
            Objects.requireNonNull(responseEntity.getBody());
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new InfrastructureException(e.getMessage());
        }
    }

    public StorageMetadata storageClientFallback(Exception e) {
        log.error(e.getMessage());
        if (cashedStorageMetadata != null) {
            log.info("'storage-client' circuit-breaker fallback is active. Getting cashed storage metadata.");
            return cashedStorageMetadata;
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "'storage-client' circuit-breaker fallback method couldn't get cashed storage metadata.", e);
        }
    }
}

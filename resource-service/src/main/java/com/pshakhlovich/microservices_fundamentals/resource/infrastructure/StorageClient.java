package com.pshakhlovich.microservices_fundamentals.resource.infrastructure;

import com.pshakhlovich.microservices_fundamentals.resource.dto.StorageMetadataDto;
import com.pshakhlovich.microservices_fundamentals.resource.dto.StorageMetadataDto.StorageType;
import com.pshakhlovich.microservices_fundamentals.resource.infrastructure.config.ExternalClientProperties;
import com.pshakhlovich.microservices_fundamentals.resource.infrastructure.exception.InfrastructureException;
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

    private StorageMetadataDto cashedStorageMetadata;

    @CircuitBreaker(name = "storage-client", fallbackMethod = "stagingStorageFallback")
    @Retry(name = "storage-client")
    public StorageMetadataDto getStagingStorage() {
        List<StorageMetadataDto> result = getAllStoragesMetadata();
        Optional<StorageMetadataDto> stagingStorageOptional = result.stream()
                .filter(storageMetadataDto -> storageMetadataDto.getStorageType().equals(StorageType.STAGING))
                .findAny();
        if (stagingStorageOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No staging storages were found.");
        }
        cashedStorageMetadata = stagingStorageOptional.get();
        return stagingStorageOptional.get();
    }

    @CircuitBreaker(name = "storage-client", fallbackMethod = "getStorageByIdFallback")
    @Retry(name = "storage-client")
    public StorageMetadataDto getStorageById(Integer storageId) {
        Optional<StorageMetadataDto> storageMetadataOptional = getAllStoragesMetadata().stream()
                .filter(storageMetadataDto -> storageMetadataDto.getId().equals(storageId))
                .findFirst();
        if (storageMetadataOptional.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "StorageMetadata was not found with id=" + storageId);
        }
        return storageMetadataOptional.get();
    }

    private List<StorageMetadataDto> getAllStoragesMetadata() throws InfrastructureException {
        try {
            ServiceInstance storageServiceInstance = loadBalancer.choose(clientProperties.getStorageServiceId());
            var baseUrl = storageServiceInstance.getUri().toString();

            ResponseEntity<List<StorageMetadataDto>> responseEntity =
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

    public StorageMetadataDto stagingStorageFallback(Exception e) {
        if (cashedStorageMetadata != null) {
            log.info("'storage-client' circuit-breaker fallback is active. Getting cashed storage metadata.");
            return cashedStorageMetadata;
        } else {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "'storage-client' circuit-breaker fallback method couldn't get cashed storage metadata.", e);
        }
    }

    public StorageMetadataDto getStorageByIdFallback(Exception e) {
        log.error("'storage-client' circuit-breaker fallback is active. Storage-service error occurred.");
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "'storage-client' error occurred.", e);
    }
}

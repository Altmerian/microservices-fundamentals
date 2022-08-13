package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.storage;

import com.pshakhlovich.microservices_fundamentals.resource.processor.dto.StorageMetadataDto;
import com.pshakhlovich.microservices_fundamentals.resource.processor.dto.StorageMetadataDto.StorageType;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.config.ExternalClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class StorageClient {

    private static final String STORAGE_BASE_URL = "/storages";

    private final ExternalClientProperties clientProperties;

    private final LoadBalancerClient loadBalancer;

    public StorageMetadataDto getPermanentStorage() {
        List<StorageMetadataDto> result = getAllStoragesMetadata();
        return result.stream()
                .filter(storageMetadataDto -> storageMetadataDto.getStorageType().equals(StorageType.PERMANENT))
                .findAny().get();
    }

    private List<StorageMetadataDto> getAllStoragesMetadata() throws RestClientException {
        ServiceInstance songServiceInstance = loadBalancer.choose(clientProperties.getStorageServiceId());
        var baseUrl = songServiceInstance.getUri().toString();

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

    }
}

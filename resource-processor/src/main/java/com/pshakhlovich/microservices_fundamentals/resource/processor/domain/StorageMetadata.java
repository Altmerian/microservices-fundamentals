package com.pshakhlovich.microservices_fundamentals.resource.processor.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageMetadata {

    private Integer id;
    private StorageType storageType;
    private String bucket;
    private String path;

    public enum StorageType {
        STAGING, PERMANENT
    }
}

package com.pshakhlovich.microservices_fundamentals.storage.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class StorageMetadata {

    private Integer id;
    private String storageType;
    private String bucket;
    private String path;
}

package com.pshakhlovich.microservices_fundamentals.resource.dto;

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
public class ReUploadDto {

    private Integer resourceId;
    private StorageMetadataDto storageMetadata;
}

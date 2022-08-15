package com.pshakhlovich.microservices_fundamentals.resource.processor.dto;

import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.StorageMetadata;
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
    private StorageMetadata storageMetadata;
}

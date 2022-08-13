package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.resource;

import com.pshakhlovich.microservices_fundamentals.resource.processor.dto.ReUploadDto;
import org.springframework.core.io.ByteArrayResource;

public interface ResourceClient {
    ByteArrayResource getResource(Integer id);

    void reUpload(ReUploadDto reUploadDto);
}

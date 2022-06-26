package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.resource;

import org.springframework.core.io.ByteArrayResource;

public interface ResourceServiceClient {
    ByteArrayResource getResource(Integer id);
}

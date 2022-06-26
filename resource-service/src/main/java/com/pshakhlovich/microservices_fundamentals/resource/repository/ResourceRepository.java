package com.pshakhlovich.microservices_fundamentals.resource.repository;

import com.pshakhlovich.microservices_fundamentals.resource.model.ResourceMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourceRepository extends JpaRepository<ResourceMetadata, Integer> {

  Optional<ResourceMetadata> findByFileName(String fileName);
}

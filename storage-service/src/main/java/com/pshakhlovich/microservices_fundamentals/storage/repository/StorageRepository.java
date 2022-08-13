package com.pshakhlovich.microservices_fundamentals.storage.repository;

import com.pshakhlovich.microservices_fundamentals.storage.model.StorageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageRepository extends JpaRepository<StorageMetadata, Integer> {}

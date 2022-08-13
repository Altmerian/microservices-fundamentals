package com.pshakhlovich.microservices_fundamentals.storage.service;

import com.pshakhlovich.microservices_fundamentals.storage.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.storage.model.StorageMetadata;
import com.pshakhlovich.microservices_fundamentals.storage.repository.StorageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

  private final StorageRepository storageRepository;

  public List<StorageMetadata> fetchAllMetadata() {
    return storageRepository.findAll();
  }

  public Integer createMetadata(StorageMetadata storageMetadata) {
    return storageRepository.saveAndFlush(storageMetadata).getId();
  }

  public IdWrapper<List<Integer>> delete(List<Integer> ids) {
    List<StorageMetadata> storagesToDelete = storageRepository.findAllById(ids);
    if (storagesToDelete.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "There are no storages with provided ids: " + ids);
    }
    var idsToDelete = storagesToDelete.stream().map(StorageMetadata::getId).collect(Collectors.toList());
    storageRepository.deleteAllById(idsToDelete);
    return new IdWrapper<>(idsToDelete);
  }
}

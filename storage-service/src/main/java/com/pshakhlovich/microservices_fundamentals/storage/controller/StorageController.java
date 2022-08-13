package com.pshakhlovich.microservices_fundamentals.storage.controller;

import com.pshakhlovich.microservices_fundamentals.storage.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.storage.model.StorageMetadata;
import com.pshakhlovich.microservices_fundamentals.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/storages")
@RequiredArgsConstructor
public class StorageController {

  private final StorageService storageService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<StorageMetadata> getAllStorageMetadata() {
    return storageService.fetchAllMetadata();
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public IdWrapper<Integer> createStorageMetadata(@RequestBody StorageMetadata storageMetadata) {
    return new IdWrapper<>(storageService.createMetadata(storageMetadata));
  }

  @DeleteMapping(value = "/{ids}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<IdWrapper<List<Integer>>> deleteStorageMetadata(
      @PathVariable @NotBlank @Size(max = 200) List<Integer> ids) {
    IdWrapper<List<Integer>> deletedIds = storageService.delete(ids);
    if (ids.size() > deletedIds.ids().size()) {
      return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(deletedIds);
    }
    return ResponseEntity.ok(deletedIds);
  }
}

package com.pshakhlovich.microservices_fundamentals.resource.controller;

import com.pshakhlovich.microservices_fundamentals.resource.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

  private final ResourceService resourceService;

  @GetMapping("/{id}")
  public ResponseEntity<ByteArrayResource> download(@PathVariable Integer id) {
    var data = resourceService.download(id);
    return ResponseEntity.ok()
        .contentLength(data.length)
        .header("Content-type", "audio/mpeg")
        .body(new ByteArrayResource(data));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public IdWrapper<Integer> uploadImage(@RequestParam("file") MultipartFile multipartFile) {
    Integer resourceId = resourceService.upload(multipartFile);
    return new IdWrapper<>(resourceId);
  }

  @DeleteMapping("/{ids}")
  public ResponseEntity<IdWrapper<int[]>> delete(
      @PathVariable @NotBlank @Size(max = 200) List<Integer> ids) {
    IdWrapper<int[]> deletedIds = resourceService.delete(ids);
    if (ids.size() > deletedIds.ids().length) {
      return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(deletedIds);
    }
    return ResponseEntity.ok(deletedIds);
  }
}

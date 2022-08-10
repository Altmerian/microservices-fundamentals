package com.pshakhlovich.microservices_fundamentals.resource.controller;

import com.pshakhlovich.microservices_fundamentals.resource.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pshakhlovich.microservices_fundamentals.resource.util.Constants.AUDIO_CONTENT_TYPE;
import static com.pshakhlovich.microservices_fundamentals.resource.util.Constants.EMULATE_TRANSIENT_ERROR_ENV_VARIABLE;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

  private final AtomicInteger failuresNumber = new AtomicInteger(0);

  private final ResourceService resourceService;

  private final Environment environment;

  @GetMapping(value = "/{id}", produces = AUDIO_CONTENT_TYPE)
  public ResponseEntity<ByteArrayResource> download(@PathVariable Integer id) {
    if ((Boolean.parseBoolean(environment.getProperty(EMULATE_TRANSIENT_ERROR_ENV_VARIABLE)))) {
      log.info("Emulating internal server error. Failure number=" + failuresNumber.get());
      return emulateTransientFailure(id);
    }
    return getResource(id);
  }

  private ResponseEntity<ByteArrayResource> emulateTransientFailure(Integer id) {
    if (failuresNumber.getAndIncrement() < 2) {
      return ResponseEntity.internalServerError().build();
    }
    failuresNumber.set(0);
    return getResource(id);
  }

  private ResponseEntity<ByteArrayResource> getResource(Integer id) {
    var data = resourceService.download(id);
    return ResponseEntity.ok()
            .contentLength(data.length)
            .header("Content-type", AUDIO_CONTENT_TYPE)
            .body(new ByteArrayResource(data));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public IdWrapper<Integer> upload(@RequestParam("file") MultipartFile multipartFile) {
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

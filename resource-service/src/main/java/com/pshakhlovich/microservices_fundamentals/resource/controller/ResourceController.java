package com.pshakhlovich.microservices_fundamentals.resource.controller;

import com.pshakhlovich.microservices_fundamentals.resource.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public IdWrapper uploadImage(@RequestParam("file") MultipartFile multipartFile) {
        Integer resourceId = resourceService.upload(multipartFile);
        return new IdWrapper(resourceId);
    }
}

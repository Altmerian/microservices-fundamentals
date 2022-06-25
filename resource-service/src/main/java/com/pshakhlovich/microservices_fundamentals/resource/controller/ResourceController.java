package com.pshakhlovich.microservices_fundamentals.resource.controller;

import com.pshakhlovich.microservices_fundamentals.resource.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public IdWrapper uploadImage(@RequestParam("file") MultipartFile multipartFile) {
        Integer resourceId = resourceService.upload(multipartFile);
        return new IdWrapper(resourceId);
    }
}

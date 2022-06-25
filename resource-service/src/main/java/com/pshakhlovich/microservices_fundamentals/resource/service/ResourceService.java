package com.pshakhlovich.microservices_fundamentals.resource.service;

import com.pshakhlovich.microservices_fundamentals.resource.config.Mp3BucketProperties;
import com.pshakhlovich.microservices_fundamentals.resource.model.ResourceMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.service.repository.ResourceRepository;
import com.pshakhlovich.microservices_fundamentals.resource.validator.Mp3FileValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResourceService {

  private final Mp3BucketProperties mp3BucketProperties;
  private final S3Client s3Client;
  private final ResourceRepository resourceRepository;
  private final Mp3FileValidator mp3FileValidator = new Mp3FileValidator();

  public Integer upload(MultipartFile multipartFile) {
    mp3FileValidator.checkContentIsValidMp3File(multipartFile);

    var originalFilename = multipartFile.getOriginalFilename();
    resourceRepository
        .findByFileName(originalFilename)
        .ifPresent(
            metadata -> {
              throw new ResponseStatusException(
                  HttpStatus.BAD_REQUEST,
                  String.format("Audio file with name '%s' already exists", originalFilename));
            });

    try {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(mp3BucketProperties.getBucketName())
              .key(originalFilename)
              .build(),
          RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));

      String fileExtension = FilenameUtils.getExtension(originalFilename);
      var resourceMetadata =
          ResourceMetadata.builder()
              .fileExtension(fileExtension)
              .fileName(originalFilename)
              .sizeInBytes(multipartFile.getSize())
              .creationTime(LocalDateTime.now())
              .build();
      return resourceRepository.saveAndFlush(resourceMetadata).getId();

    } catch (IOException | S3Exception e) {
      //            delete(multipartFile.getOriginalFilename());
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}

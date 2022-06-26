package com.pshakhlovich.microservices_fundamentals.resource.service;

import com.pshakhlovich.microservices_fundamentals.resource.config.Mp3BucketProperties;
import com.pshakhlovich.microservices_fundamentals.resource.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.model.ResourceMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.service.repository.ResourceRepository;
import com.pshakhlovich.microservices_fundamentals.resource.validator.Mp3FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

  public byte[] download(Integer resourceId) {
    try {
      var resourceMetadata =
          resourceRepository
              .findById(resourceId)
              .orElseThrow(
                  () ->
                      new ResponseStatusException(
                          HttpStatus.NOT_FOUND,
                          String.format("Resource with id=%d not found", resourceId)));

      GetObjectRequest objectRequest =
          GetObjectRequest.builder()
              .key(resourceMetadata.getFileName())
              .bucket(mp3BucketProperties.getBucketName())
              .build();

      return s3Client.getObjectAsBytes(objectRequest).asByteArray();

    } catch (S3Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @PostConstruct
  private void createBucket() {
    var bucketName = mp3BucketProperties.getBucketName();
    if (!checkIfBucketExists(bucketName)) {
      try {
        var s3Waiter = s3Client.waiter();
        CreateBucketRequest bucketRequest =
            CreateBucketRequest.builder().bucket(bucketName).build();

        s3Client.createBucket(bucketRequest);
        HeadBucketRequest bucketRequestWait =
            HeadBucketRequest.builder().bucket(bucketName).build();

        // Wait until the bucket is created and print out the response.
        WaiterResponse<HeadBucketResponse> waiterResponse =
            s3Waiter.waitUntilBucketExists(bucketRequestWait);
        waiterResponse.matched().response().ifPresent(System.out::println);
        log.info(bucketName + " bucket has been created");

      } catch (S3Exception e) {
        System.err.println(e.awsErrorDetails().errorMessage());
      }
    }
  }

  private boolean checkIfBucketExists(String bucketName) {
    HeadBucketRequest headBucketRequest = HeadBucketRequest.builder().bucket(bucketName).build();
    try {
      s3Client.headBucket(headBucketRequest);
      return true;
    } catch (NoSuchBucketException e) {
      return false;
    }
  }

  @Transactional
  public IdWrapper<int[]> delete(@Size List<Integer> ids) {

    List<ResourceMetadata> resourcesToDelete = resourceRepository.findAllById(ids);
    if (resourcesToDelete.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "There are no resources with provided ids: " + ids);
    }

    var idsByFileNamesToDelete =
        resourcesToDelete.stream()
            .collect(Collectors.toMap(ResourceMetadata::getFileName, ResourceMetadata::getId));
    List<ObjectIdentifier> keys =
        resourcesToDelete.stream()
            .map(
                resourceMetadata ->
                    ObjectIdentifier.builder().key(resourceMetadata.getFileName()).build())
            .toList();
    var delete = Delete.builder().objects(keys).build();

    try {
      var multiObjectDeleteRequest =
          DeleteObjectsRequest.builder()
              .bucket(mp3BucketProperties.getBucketName())
              .delete(delete)
              .build();
      var deleteObjectsResponse = s3Client.deleteObjects(multiObjectDeleteRequest);
      resourceRepository.deleteAllById(idsByFileNamesToDelete.values());
      return new IdWrapper<>(
          deleteObjectsResponse.deleted().stream()
              .map(DeletedObject::key)
              .mapToInt(idsByFileNamesToDelete::get)
              .sorted()
              .toArray());
    } catch (S3Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }
}

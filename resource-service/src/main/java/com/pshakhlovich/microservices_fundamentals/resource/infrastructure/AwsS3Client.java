package com.pshakhlovich.microservices_fundamentals.resource.infrastructure;

import com.pshakhlovich.microservices_fundamentals.resource.config.Mp3BucketProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AwsS3Client {

  private final Mp3BucketProperties mp3BucketProperties;
  private final S3Client s3Client;

  public void uploadFile(MultipartFile multipartFile) throws IOException {
    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(mp3BucketProperties.getBucketName())
            .key(multipartFile.getOriginalFilename())
            .build(),
        RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
  }

  public byte[] downloadFile(String fileName) {
    try {
      var getObjectRequest =
          GetObjectRequest.builder()
              .key(fileName)
              .bucket(mp3BucketProperties.getBucketName())
              .build();

      return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
    } catch (S3Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  public List<String> removeFiles(Collection<String> fileNames) {
    List<ObjectIdentifier> keys =
        fileNames.stream()
            .map(fileName -> ObjectIdentifier.builder().key(fileName).build())
            .toList();
    var delete = Delete.builder().objects(keys).build();

    try {
      var multiObjectDeleteRequest =
          DeleteObjectsRequest.builder()
              .bucket(mp3BucketProperties.getBucketName())
              .delete(delete)
              .build();
      var deleteObjectsResponse = s3Client.deleteObjects(multiObjectDeleteRequest);

      return deleteObjectsResponse.deleted().stream()
          .map(DeletedObject::key)
          .collect(Collectors.toList());
    } catch (S3Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  @PostConstruct
  public void createBucketIfNotExists() {
    var bucketName = mp3BucketProperties.getBucketName();
    if (!checkIfBucketExists(bucketName)) {
      try {
        var s3Waiter = s3Client.waiter();
        CreateBucketRequest bucketRequest =
            CreateBucketRequest.builder().bucket(bucketName).build();

        s3Client.createBucket(bucketRequest);
        HeadBucketRequest bucketRequestWait =
            HeadBucketRequest.builder().bucket(bucketName).build();

        // Wait until the bucket is created and log the response.
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
}

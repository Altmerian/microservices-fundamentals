package com.pshakhlovich.microservices_fundamentals.resource.infrastructure;

import com.pshakhlovich.microservices_fundamentals.resource.config.Mp3BucketProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeletedObject;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AwsS3Client {

  private final Mp3BucketProperties mp3BucketProperties;
  private final S3Client s3Client;

  public void uploadFile(MultipartFile multipartFile, String bucketName, String path) throws IOException {
    createBucketIfNotExists(bucketName);

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(path + multipartFile.getOriginalFilename())
            .build(),
        RequestBody.fromInputStream(multipartFile.getInputStream(), multipartFile.getSize()));
  }

  public byte[] downloadFile(String bucketName, String fileKey) {
    try {
      var getObjectRequest =
          GetObjectRequest.builder()
              .key(fileKey)
              .bucket(bucketName)
              .build();

      return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();
    } catch (S3Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.awsErrorDetails().errorMessage(), e);
    }
  }

  public List<String> removeFiles(MultiValueMap<String, String> fileKeysByBucketName) {
    List<String> deletedIds = new ArrayList<>();

    fileKeysByBucketName.forEach((bucketName, fileKeys) -> {
      List<ObjectIdentifier> keys =
              fileKeys.stream()
                      .map(fileKey -> ObjectIdentifier.builder().key(fileKey).build())
                      .toList();
      var delete = Delete.builder().objects(keys).build();

      try {
        var multiObjectDeleteRequest =
                DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(delete)
                        .build();
        var deleteObjectsResponse = s3Client.deleteObjects(multiObjectDeleteRequest);

        deletedIds.addAll(deleteObjectsResponse.deleted().stream()
                .map(DeletedObject::key).toList());
      } catch (S3Exception e) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.awsErrorDetails().errorMessage(), e);
      }
    });

    return  deletedIds;
  }

  public void createBucketIfNotExists(String bucketName) {
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
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.awsErrorDetails().errorMessage(), e);
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

  public void copyObject(String fromBucket, String fromPath, String toBucket, String toPath, String fileName) {

    CopyObjectRequest copyReq = CopyObjectRequest.builder()
            .sourceBucket(fromBucket)
            .sourceKey(fromPath + fileName)
            .destinationBucket(toBucket)
            .destinationKey(toPath + fileName)
            .build();

    try {
     s3Client.copyObject(copyReq);
     removeFiles(new LinkedMultiValueMap<>(Map.of(fromBucket, List.of(fromPath + fileName))));

    } catch (S3Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.awsErrorDetails().errorMessage(), e);
    }
  }
}

package com.pshakhlovich.microservices_fundamentals.resource.service;

import com.pshakhlovich.microservices_fundamentals.resource.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.event.EventPublisher;
import com.pshakhlovich.microservices_fundamentals.resource.infrastructure.AwsS3Client;
import com.pshakhlovich.microservices_fundamentals.resource.model.ResourceMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.repository.ResourceRepository;
import com.pshakhlovich.microservices_fundamentals.resource.validator.Mp3FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.Size;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResourceService {

  private final AwsS3Client awsS3Client;
  private final ResourceRepository resourceRepository;
  private final Mp3FileValidator mp3FileValidator = new Mp3FileValidator();
  private final EventPublisher eventPublisher;

  public Integer upload(MultipartFile multipartFile) {
    mp3FileValidator.checkContentIsValidMp3File(multipartFile);

    var originalFilename = multipartFile.getOriginalFilename();
    resourceRepository
        .findByFileName(originalFilename)
        .ifPresent(
            metadata -> {
              throw new ResponseStatusException(
                  HttpStatus.CONFLICT,
                  String.format("Audio file with name '%s' already exists", originalFilename));
            });

    try {
      awsS3Client.uploadFile(multipartFile);

      String fileExtension = FilenameUtils.getExtension(originalFilename);
      var resourceMetadata =
          ResourceMetadata.builder()
              .fileExtension(fileExtension)
              .fileName(originalFilename)
              .sizeInBytes(multipartFile.getSize())
              .creationTime(LocalDateTime.now())
              .build();
      Integer resourceId = resourceRepository.saveAndFlush(resourceMetadata).getId();
      eventPublisher.publish(resourceMetadata);
      return resourceId;

    } catch (IOException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  public byte[] download(Integer resourceId) {
    var resourceMetadata =
        resourceRepository
            .findById(resourceId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        String.format("Resource with id=%d not found", resourceId)));

    return awsS3Client.downloadFile(resourceMetadata.getFileName());
  }

  public IdWrapper<int[]> delete(@Size List<Integer> ids) {

    List<ResourceMetadata> resourcesToDelete = resourceRepository.findAllById(ids);
    if (resourcesToDelete.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "There are no resources with provided ids: " + ids);
    }

    var idsByFileNamesToDelete =
        resourcesToDelete.stream()
            .collect(Collectors.toMap(ResourceMetadata::getFileName, ResourceMetadata::getId));

    resourceRepository.deleteAllById(idsByFileNamesToDelete.values());

    List<String> removedObjectKeys = awsS3Client.removeFiles(idsByFileNamesToDelete.keySet());

    return new IdWrapper<>(
        removedObjectKeys.stream().mapToInt(idsByFileNamesToDelete::get).sorted().toArray());
  }
}

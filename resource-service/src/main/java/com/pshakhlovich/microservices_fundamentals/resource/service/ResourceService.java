package com.pshakhlovich.microservices_fundamentals.resource.service;

import com.pshakhlovich.microservices_fundamentals.resource.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.dto.ReUploadDto;
import com.pshakhlovich.microservices_fundamentals.resource.dto.StorageMetadataDto;
import com.pshakhlovich.microservices_fundamentals.resource.event.EventPublisher;
import com.pshakhlovich.microservices_fundamentals.resource.infrastructure.AwsS3Client;
import com.pshakhlovich.microservices_fundamentals.resource.infrastructure.StorageClient;
import com.pshakhlovich.microservices_fundamentals.resource.model.ResourceMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.repository.ResourceRepository;
import com.pshakhlovich.microservices_fundamentals.resource.validator.Mp3FileValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.validation.constraints.Size;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResourceService {

    private final AwsS3Client awsS3Client;
    private final StorageClient storageClient;
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

        StorageMetadataDto stagingStorage = storageClient.getStagingStorage();

        try {
            awsS3Client.uploadFile(multipartFile, stagingStorage.getBucket(), stagingStorage.getPath());

            String fileExtension = FilenameUtils.getExtension(originalFilename);
            var resourceMetadata =
                    ResourceMetadata.builder()
                            .fileExtension(fileExtension)
                            .fileName(originalFilename)
                            .sizeInBytes(multipartFile.getSize())
                            .creationTime(LocalDateTime.now())
                            .storageId(stagingStorage.getId())
                            .build();
            Integer resourceId = resourceRepository.saveAndFlush(resourceMetadata).getId();
            eventPublisher.publish(resourceMetadata);
            log.info("Resource with id={} was uploaded to the staging storage with id={}",
                    resourceMetadata.getId(), resourceMetadata.getStorageId());
            return resourceId;

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    public void reUpload(ReUploadDto reUploadDto) {
        ResourceMetadata resourceMetadata = resourceRepository.getReferenceById(reUploadDto.getResourceId());

        try {
            StorageMetadataDto stagingStorage = storageClient.getStorageById(resourceMetadata.getStorageId());

            awsS3Client.copyObject(
                    stagingStorage.getBucket(), stagingStorage.getPath(),
                    reUploadDto.getStorageMetadata().getBucket(), reUploadDto.getStorageMetadata().getPath(),
                    resourceMetadata.getFileName());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        resourceMetadata.setStorageId(reUploadDto.getStorageMetadata().getId());
        resourceRepository.saveAndFlush(resourceMetadata);
        log.info("Resource with id={} was re-uploaded to the permanent storage with id={}",
                resourceMetadata.getId(), resourceMetadata.getStorageId());
    }

    public byte[] download(Integer resourceId) {
        var resourceMetadata =
                resourceRepository
                        .findById(resourceId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                String.format("Resource with id=%d not found", resourceId)));

        var storageMetadata = storageClient.getStorageById(resourceMetadata.getStorageId());

        try {
            return awsS3Client.downloadFile(storageMetadata.getBucket(), storageMetadata.getPath() + resourceMetadata.getFileName());
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    public IdWrapper<int[]> delete(@Size List<Integer> ids) {

        List<ResourceMetadata> resourcesToDelete = resourceRepository.findAllById(ids);
        if (resourcesToDelete.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "There are no resources with provided ids: " + ids);
        }

        var idsByFileKeysToDelete = new HashMap<String, Integer>();

        MultiValueMap<String, String> fileKeysByBucketName = new LinkedMultiValueMap<>();

        resourcesToDelete.forEach(resourceMetadata -> {
            var storageMetadataDto = storageClient.getStorageById(resourceMetadata.getStorageId());
            String fileKey = storageMetadataDto.getPath() + resourceMetadata.getFileName();
            idsByFileKeysToDelete.put(fileKey, resourceMetadata.getId());
            fileKeysByBucketName.add(storageMetadataDto.getBucket(), fileKey);
        });

        resourceRepository.deleteAllById(idsByFileKeysToDelete.values());

        List<String> removedObjectKeys = awsS3Client.removeFiles(fileKeysByBucketName);

        return new IdWrapper<>(
                removedObjectKeys.stream()
                        .mapToInt(idsByFileKeysToDelete::get)
                        .sorted()
                        .toArray());
    }
}

package com.pshakhlovich.microservices_fundamentals.resource.processor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.ResourceMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.SongMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.StorageMetadata.StorageType;
import com.pshakhlovich.microservices_fundamentals.resource.processor.dto.ReUploadDto;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.StorageMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.resource.ResourceClient;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.song.SongClient;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.storage.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceProcessorService {

  private final ResourceClient resourceClient;
  private final SongClient songClient;
  private final StorageClient storageClient;
  private final ObjectMapper objectMapper;

  private final CountDownLatch latch = new CountDownLatch(1);

  @Transactional
  @KafkaListener(
      id = "res-process-1",
      topics = "${kafka.resource-topic}",
      containerFactory = "kafkaListenerContainerFactory")
  public void processUploadEvent(String message) throws JsonProcessingException {

    log.info("The following message was received from the resource-upload topic: " + message);
    var resourceId = objectMapper.readValue(message, ResourceMetadata.class).getId();

    ByteArrayResource resource = resourceClient.getResource(resourceId);

    SongMetadata songMetadata = extractSongMetadata(resourceId, resource);

    var songMetadataId = songClient.storeSongMetadata(songMetadata);
    log.info("Song metadata has been persisted with id=" + songMetadataId);

    StorageMetadata permanentStorage = getPermanentStorage();
    resourceClient.reUpload(
            ReUploadDto.builder()
                    .resourceId(resourceId)
                    .storageMetadata(permanentStorage)
            .build());

    latch.countDown();
  }

  private SongMetadata extractSongMetadata(Integer resourceId, ByteArrayResource resource) {
    ContentHandler handler = new DefaultHandler();
    Metadata metadata = new Metadata();
    Parser parser = new Mp3Parser();
    ParseContext parseCtx = new ParseContext();

    try (var inputStream = resource.getInputStream()) {
      parser.parse(inputStream, handler, metadata, parseCtx);
    } catch (IOException | SAXException | TikaException e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    log.info("Extracted song metadata: {}", Arrays.toString(metadata.names()));

    return SongMetadata.builder()
        .name(metadata.get("dc:title"))
        .artist(metadata.get("xmpDM:artist"))
        .album(metadata.get("xmpDM:album"))
        .length(formatDuration(metadata.get("xmpDM:duration")))
        .resourceId(resourceId)
        .year(getYear(metadata))
        .build();
  }

  private StorageMetadata getPermanentStorage() {
    List<StorageMetadata> result = storageClient.getAllStoragesMetadata();

    Optional<StorageMetadata> permanentStorageOptional = result.stream()
            .filter(storageMetadataDto -> storageMetadataDto.getStorageType().equals(StorageType.PERMANENT))
            .findAny();
    if (permanentStorageOptional.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Permanent storage metadata wasn't found.");
    }
    return permanentStorageOptional.get();
  }

  private String formatDuration(String input) {
    if (input != null) {
      double doubleDuration = Double.parseDouble(input);
      var durationInMillis = (long) (doubleDuration * 1000);
      return LocalTime.MIDNIGHT
          .plus(Duration.ofMillis(durationInMillis))
          .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    return null;
  }

  private Integer getYear(Metadata metadata) {
    return (metadata.get("xmpDM:releaseDate") != null)
        ? Integer.valueOf(metadata.get("xmpDM:releaseDate"))
        : null;
  }

  public CountDownLatch getLatch() {
    return latch;
  }
}

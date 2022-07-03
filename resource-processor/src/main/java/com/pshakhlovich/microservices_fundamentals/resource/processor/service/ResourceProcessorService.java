package com.pshakhlovich.microservices_fundamentals.resource.processor.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.ResourceMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.SongMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.resource.ResourceClient;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.song.SongClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceProcessorService {

  private final ResourceClient resourceClient;
  private final SongClient songClient;
  private final ObjectMapper objectMapper;

  @Transactional
  @KafkaListener(
      id = "res-process-1",
      topics = "resource-upload",
      properties = {
              ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG + " localhost:9093",
              ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG + " org.apache.kafka.common.serialization.IntegerDeserializer",
      })
  public void processUploadEvent(String message) throws JsonProcessingException {

    log.info("The following message was received from the resource-upload topic: " + message);
    var resourceId = objectMapper.readValue(message, ResourceMetadata.class).getId();

    ByteArrayResource resource = resourceClient.getResource(resourceId);
    ContentHandler handler = new DefaultHandler();
    Metadata metadata = new Metadata();
    Parser parser = new Mp3Parser();
    ParseContext parseCtx = new ParseContext();

    try (var inputStream = resource.getInputStream()) {
      parser.parse(inputStream, handler, metadata, parseCtx);
    } catch (IOException | SAXException | TikaException e) {
      throw new RuntimeException(e);
    }

    log.info("Extracted song metadata: {}", Arrays.toString(metadata.names()));

    var songMetadata =
        SongMetadata.builder()
            .name(metadata.get("dc:title"))
            .artist(metadata.get("xmpDM:artist"))
            .album(metadata.get("xmpDM:album"))
            .length(formatDuration(metadata.get("xmpDM:duration")))
            .resourceId(resourceId)
            .year(Integer.valueOf(metadata.get("xmpDM:releaseDate")))
            .build();

    var songMetadataId = songClient.storeSongMetadata(songMetadata);
    log.info("Song metadata has been persisted with id=" + songMetadataId);
  }

  private String formatDuration(String input) {
    double doubleDuration = Double.parseDouble(input);
    var durationInMillis = (long)(doubleDuration * 1000);
    return LocalTime.MIDNIGHT.plus(Duration.ofMillis(durationInMillis)).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
  }
}

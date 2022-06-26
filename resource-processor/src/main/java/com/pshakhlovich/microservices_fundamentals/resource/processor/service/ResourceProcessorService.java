package com.pshakhlovich.microservices_fundamentals.resource.processor.service;

import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.SongMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.resource.ResourceServiceClient;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.song.SongServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceProcessorService {

  private ResourceServiceClient resourceServiceClient;
  private SongServiceClient songServiceClient;

  public void processUploadEvent(Integer resourceId) {
    ByteArrayResource resource = resourceServiceClient.getResource(resourceId);
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
            .name(metadata.get("title"))
            .artist(metadata.get("xmpDM:artist"))
            .album(metadata.get("xmpDM:album"))
            .resourceId(resourceId)
            .build();

    songServiceClient.storeSongMetadata(songMetadata);
  }
}

package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.song;

import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.SongMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.config.ExternalClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Component
public class SongClientImpl implements SongClient {

  private final ExternalClientProperties clientProperties;

  private static final String SONG_BASE_URL = "/songs";

  @Autowired
  public SongClientImpl(ExternalClientProperties clientProperties) {
    this.clientProperties = clientProperties;
  }

  @Override
  public Integer storeSongMetadata(SongMetadata songMetadata) {
    HttpEntity<SongMetadata> entity = new HttpEntity<>(songMetadata);
    try {
      ResponseEntity<IdWrapper> responseEntity =
              new RestTemplate().postForEntity(clientProperties.getSongServiceUrl() + SONG_BASE_URL, entity, IdWrapper.class);
      var idWrapper = Objects.requireNonNull(responseEntity.getBody());

      return idWrapper.ids();

    } catch (RestClientException e) {
      throw new ResponseStatusException(
              HttpStatus.INTERNAL_SERVER_ERROR, "'song-service' failed to store Song Metadata", e);
    }
  }
}

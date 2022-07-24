package com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.SongMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.dto.IdWrapper;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.config.ExternalClientProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = ExternalClientProperties.class)
@AutoConfigureStubRunner(
    stubsMode = StubRunnerProperties.StubsMode.LOCAL,
    ids = "com.pshakhlovich:song-service:+:stubs:8082")
@ActiveProfiles("test")
class SongClientContractTest {

  private static final String SONG_BASE_URI = "http://localhost:8082/songs";
  private static final int SONG_ID = 1;

  private final RestTemplate restTemplate = new RestTemplate();

  @Test
  void pingStub() throws Exception {
    ResponseEntity<Void> response =
        restTemplate.getForEntity("http://localhost:8082/ping", Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void storeSongMetadata() throws Exception {
    // given
    var songMetadata = getSong();

    // when
    var response = restTemplate.postForEntity(SONG_BASE_URI, songMetadata, IdWrapper.class);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().ids()).isEqualTo(SONG_ID);
  }

  private SongMetadata getSong() {
    return SongMetadata.builder()
        .album("News of the world")
        .artist("Queen")
        .name("We are the champions")
        .length("2:59")
        .resourceId(1)
        .year(1977)
        .build();
  }
}

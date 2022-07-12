package com.pshakhlovich.microservices_fundamentals.resource.processor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.processor.config.KafkaProperties;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.ResourceMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.domain.SongMetadata;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.resource.ResourceClient;
import com.pshakhlovich.microservices_fundamentals.resource.processor.infrastructure.song.SongClient;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://${kafka.bootstrap-address}", "port=9092"},
    topics = "${kafka.resource-topic}"
)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ExtendWith({OutputCaptureExtension.class, MockitoExtension.class})
@DirtiesContext
class ResourceProcessorListenerIT {

  private static final String TEST_MP3_FILE_PATH = "test_data/file_example_MP3_5MG.mp3";

  private static final int RESOURCE_ID = 1;
  private static final Integer SONG_METADATA_ID = 1;

  private static final String LOG_MESSAGE_RECEIVED =
      "The following message was received from the resource-upload topic: ";

  private static final String LOG_SONG_METADATA_PERSISTED =
          "Song metadata has been persisted with id=" + SONG_METADATA_ID;

  @Autowired private ResourceProcessorService resourceProcessorService;

  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ResourceClient resourceClient;

  @MockBean private SongClient songClient;

  @Value("${kafka.resource-topic}")
  private String topicName;

  private Producer<Integer, String> producer;

  @TestConfiguration
  @EnableConfigurationProperties({KafkaProperties.class})
  static class ResourceProcessorTestConfiguration {

    @Bean
    ObjectMapper objectMapper() {
      return new ObjectMapper().findAndRegisterModules();
    }
  }

  @BeforeEach
  void setUp() {
    Map<String, Object> producerProps =
        new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
    producer = new DefaultKafkaProducerFactory<Integer, String>(producerProps).createProducer();
  }

  @AfterEach
  void tearDown() {
    producer.close();
  }

  @Test
  void processUploadEvent(CapturedOutput capturedOutput) throws IOException, InterruptedException {
    // given
    var resourceMetadata =
        ResourceMetadata.builder()
            .id(RESOURCE_ID)
            .fileExtension("mp3")
            .fileName("exampleFile")
            .sizeInBytes(1_048_576L)
            .creationTime(LocalDateTime.now())
            .build();
    String resourceMetadataJson = objectMapper.writeValueAsString(resourceMetadata);

    var classPathResource = new ClassPathResource(TEST_MP3_FILE_PATH);
    var inputStream = classPathResource.getInputStream();

    var byteArrayResourceMock = mock(ByteArrayResource.class);
    when(resourceClient.getResource(RESOURCE_ID)).thenReturn(byteArrayResourceMock);
    when(byteArrayResourceMock.getInputStream()).thenReturn(inputStream);

    when(songClient.storeSongMetadata(any(SongMetadata.class))).thenReturn(SONG_METADATA_ID);

    // when
    producer.send(new ProducerRecord<>(topicName, 1, resourceMetadataJson));

    // then
    boolean messageConsumed = resourceProcessorService.getLatch().await(10, TimeUnit.SECONDS);
    assertTrue(messageConsumed);
    assertThat(capturedOutput.getOut())
        .contains(LOG_MESSAGE_RECEIVED + resourceMetadataJson, LOG_SONG_METADATA_PERSISTED);
  }
}

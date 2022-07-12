package com.pshakhlovich.microservices_fundamentals.resource.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.config.KafkaProperties;
import com.pshakhlovich.microservices_fundamentals.resource.model.ResourceMetadata;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://${kafka.bootstrap-address}", "port=9092"},
    topics = "${kafka.resource-topic}")
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = {EventPublisherIT.EventPublisherTestConfiguration.class, EventPublisher.class})
class EventPublisherIT {

  private static final int RESOURCE_ID = 1;

  @Autowired private EventPublisher eventPublisher;

  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired private ObjectMapper objectMapper;

  @Value("${kafka.resource-topic}")
  private String topicName;

  private Consumer<Integer, String> consumer;

  @TestConfiguration
  @EnableConfigurationProperties({KafkaProperties.class})
  static class EventPublisherTestConfiguration {

    @Bean
    ObjectMapper objectMapper() {
      return new ObjectMapper()
          .findAndRegisterModules();
    }

    @Bean
    KafkaTemplate<Integer, String> kafkaTemplate(KafkaProperties kafkaProperties) {
      Map<String, Object> configProps = new HashMap<>();
      configProps.put(
          ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapAddress());
      configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
      configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

      DefaultKafkaProducerFactory<Integer, String> kafkaProducerFactory =
          new DefaultKafkaProducerFactory<>(configProps);

      return new KafkaTemplate<>(kafkaProducerFactory);
    }
  }

  @BeforeEach
  void setUp() {
    Map<String, Object> consumerProps =
        KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumer = new DefaultKafkaConsumerFactory<Integer, String>(consumerProps).createConsumer();
    consumer.subscribe(Collections.singleton(topicName));
  }

  @AfterEach
  void tearDown() {
    consumer.close();
  }

  @Test
  void publishMessage() throws JsonProcessingException {
    // given
    var resourceMetadata =
        ResourceMetadata.builder()
            .id(RESOURCE_ID)
            .fileExtension("mp3")
            .fileName("exampleFile")
            .sizeInBytes(1_048_576L)
            .creationTime(LocalDateTime.now())
            .build();

    // when
    assertThatCode(() -> eventPublisher.publish(resourceMetadata)).doesNotThrowAnyException();

    // then
    ConsumerRecord<Integer, String> singleRecord =
        KafkaTestUtils.getSingleRecord(consumer, topicName);
    assertThat(singleRecord).isNotNull();
    assertThat(singleRecord.key()).isEqualTo(1);
    assertThat(singleRecord.value()).isEqualTo(objectMapper.writeValueAsString(resourceMetadata));
  }
}

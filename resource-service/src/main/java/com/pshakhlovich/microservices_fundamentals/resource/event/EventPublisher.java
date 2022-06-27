package com.pshakhlovich.microservices_fundamentals.resource.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.config.KafkaProperties;
import com.pshakhlovich.microservices_fundamentals.resource.model.ResourceMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventPublisher {

  private final KafkaTemplate<Integer, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final KafkaProperties kafkaProperties;

  public void publish(ResourceMetadata resourceMetadata) throws JsonProcessingException {
    final ProducerRecord<Integer, String> record = createRecord(resourceMetadata);

    ListenableFuture<SendResult<Integer, String>> future = kafkaTemplate.send(record);
    future.addCallback(
        new KafkaSendCallback<>() {
          @Override
          public void onSuccess(SendResult<Integer, String> result) {
            log.info(
                "Upload resource event message={} was published with offset={}, topic={}",
                result.getProducerRecord().value(),
                result.getRecordMetadata().offset(),
                result.getRecordMetadata().topic());
          }
          @Override
          public void onFailure(@NonNull KafkaProducerException ex) {
            log.error(ex.getMessage(), ex);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
          }
        });
  }

  private ProducerRecord<Integer, String> createRecord(ResourceMetadata resourceMetadata) throws JsonProcessingException {
    return new ProducerRecord<>(
        kafkaProperties.getResourceTopic(),
        resourceMetadata.getId(),
        objectMapper.writeValueAsString(resourceMetadata));
  }
}

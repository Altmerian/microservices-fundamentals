package com.pshakhlovich.microservices_fundamentals.resource.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pshakhlovich.microservices_fundamentals.resource.config.KafkaProperties;
import com.pshakhlovich.microservices_fundamentals.resource.model.ResourceMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventPublisher {

  private final KafkaTemplate<Integer, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final KafkaProperties kafkaProperties;

  @PostConstruct
  void init() {
    kafkaTemplate.setDefaultTopic(kafkaProperties.getResourceTopic());
  }

  public void publish(ResourceMetadata resourceMetadata) throws JsonProcessingException {
    final Message<String> message = createMessage(resourceMetadata);

    ListenableFuture<SendResult<Integer, String>> future = kafkaTemplate.send(message);
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

  private Message<String> createMessage(ResourceMetadata resourceMetadata)
      throws JsonProcessingException {
    return MessageBuilder.withPayload(objectMapper.writeValueAsString(resourceMetadata))
        .setHeader(KafkaHeaders.MESSAGE_KEY, resourceMetadata.getId())
        .build();
  }
}

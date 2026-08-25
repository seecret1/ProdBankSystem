package com.github.seecret1.order_service.kafka.producer.impl;

import com.github.seecret1.order_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.kafka.producer.OrderInnerRequestKafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderInnerRequestKafkaProducerServiceImpl implements OrderInnerRequestKafkaProducerService {

    private final KafkaProperties kafkaProperties;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, OrderCardDto> kafkaTemplate;

    @Override
    public void sendNoWait(OrderCardDto dto) {
        logging(dto);
        ProducerRecord<String, OrderCardDto> record = getRecord(kafkaProperties.getInnerTopic(), dto);
        kafkaTemplate.send(record);
    }

    @Override
    public void sendWithWait(OrderCardDto dto) {
        kafkaRetryTemplate.execute(context -> {
            try {
                logging(dto);

                ProducerRecord<String, OrderCardDto> record =
                        getRecord(kafkaProperties.getInnerTopic(), dto);
                kafkaTemplate.send(record).get(kafkaProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
                return null;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to send message to Kafka", e);
                throw new IllegalStateException("Interrupted while waiting for Kafka send", e);
            } catch (ExecutionException | TimeoutException ex) {
                log.error("Failed to send request to Kafka", ex);
                throw new RuntimeException("Failed to send request to Kafka", ex);
            }
        });
    }

    private ProducerRecord<String, OrderCardDto> getRecord(String topic, OrderCardDto message) {
        return new ProducerRecord<>(topic, message.getTraceId(), message);
    }

    private static void logging(OrderCardDto message) {
        log.debug("Response sent to Kafka: traceId={}, cardId={}, userId={}, createdAt={}",
                message.getTraceId(), message.getCardId(),
                message.getUserId(), message.getCreatedAt());
    }
}

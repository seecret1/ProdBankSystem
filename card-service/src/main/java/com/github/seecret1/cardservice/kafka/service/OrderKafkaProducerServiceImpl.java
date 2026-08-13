package com.github.seecret1.cardservice.kafka.service;

import com.github.seecret1.cardservice.config.kafka.properties.KafkaProperties;
import com.github.seecret1.cardservice.dto.order.message.OrderCardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderKafkaProducerServiceImpl implements OrderKafkaProducerService {

    private final KafkaProperties kafkaProperties;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, OrderCardDto> kafkaTemplate;

    @Override
    public void sendNoWait(OrderCardDto request) {
        loggingMessage(request);
        kafkaTemplate.send(kafkaProperties.getOrdersTopic(), request.getTraceId(), request);
    }

    @Override
    public void sendWithWait(OrderCardDto request) {
        kafkaRetryTemplate.execute(context -> {
            try {
                ProducerRecord<String, OrderCardDto> record =
                        getRecord(kafkaProperties.getOrdersTopic(), request);

                loggingMessage(request);
                kafkaTemplate.send(record).get();

                return null;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to send request to Kafka", e);
                throw new IllegalStateException("Interrupted while waiting for Kafka send", e);
            } catch (ExecutionException e) {
                log.error("Failed to send request to Kafka", e);
                throw new IllegalStateException("Failed to send message to Kafka", e);
            }
        });
    }

    private ProducerRecord<String, OrderCardDto> getRecord(String topic, OrderCardDto message) {
        return new ProducerRecord<>(topic, message.getTraceId(), message);
    }

    private static void loggingMessage(OrderCardDto message) {
        log.debug("Order message: traceId={}, userId={}, cardId={}, cardType={}, orderType={}, spendingLimit={}, comment={}, createdAt={}",
                message.getTraceId(), message.getUserId(), message.getCardId(), message.getCardType(), message.getOrderType(),
                message.getSpendingLimit(), message.getComment(), message.getCreatedAt());
    }
}

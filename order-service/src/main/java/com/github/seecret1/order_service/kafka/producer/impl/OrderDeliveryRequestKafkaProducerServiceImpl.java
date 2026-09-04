package com.github.seecret1.order_service.kafka.producer.impl;

import com.github.seecret1.order_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.order_service.dto.delivery.OrderCardDeliveryDto;
import com.github.seecret1.order_service.kafka.producer.OrderDeliveryRequestKafkaProducerService;
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
public class OrderDeliveryRequestKafkaProducerServiceImpl implements OrderDeliveryRequestKafkaProducerService {

    private final KafkaTemplate<String, OrderCardDeliveryDto> orderKafkaTemplate;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaProperties kafkaProperties;

    @Override
    public void sendRequestNoWait(OrderCardDeliveryDto deliveryDto) {
        ProducerRecord<String, OrderCardDeliveryDto> record = getRecord(kafkaProperties.getCardsTopic(), deliveryDto);
        logging(record.topic(), deliveryDto);
        orderKafkaTemplate.send(record);
    }

    @Override
    public void sendRequestWithWait(OrderCardDeliveryDto deliveryDto) {
        kafkaRetryTemplate.execute(context -> {
            try {
                ProducerRecord<String, OrderCardDeliveryDto> record =
                        getRecord(kafkaProperties.getDeliveryTopic(), deliveryDto);

                logging(record.topic(), deliveryDto);
                orderKafkaTemplate.send(record).get(kafkaProperties.getTimeoutSeconds(), TimeUnit.SECONDS);
                return null;

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Failed to send request to Kafka", e);
                throw new RuntimeException("Interrupted while waiting for Kafka send", e);
            } catch (ExecutionException | TimeoutException ex) {
                log.error("Failed to send request to Kafka", ex);
                throw new RuntimeException("Failed to send request to Kafka", ex);
            }
        });
    }

    private ProducerRecord<String, OrderCardDeliveryDto> getRecord(String topic, OrderCardDeliveryDto deliveryDto) {
        return new ProducerRecord<>(topic, deliveryDto.getTraceId(), deliveryDto);
    }

    private static void logging(String topic, OrderCardDeliveryDto deliveryDto) {
        log.info("Delivery request sent to Kafka: topic={}, traceId={}, orderId={}, officeId={}, originalAddress={}, destinationAddress={}",
                topic, deliveryDto.getTraceId(), deliveryDto.getOrderId(), deliveryDto.getOfficeId(),
                deliveryDto.getOriginAddress(), deliveryDto.getDestinationAddress());
    }
}

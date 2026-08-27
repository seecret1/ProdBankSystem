package com.github.seecret1.invoice_service.kafka.producer;

import com.github.seecret1.invoice_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.invoice_service.dto.order.BaseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMessageOrderResponseKafkaProducerServiceImpl implements OrderMessageKafkaProducerService {

    private final KafkaProperties kafkaProperties;

    private final RetryTemplate kafkaRetryTemplate;

    private final KafkaTemplate<String, BaseMessage> kafkaTemplate;

    @Override
    public void sendNoWait(String topic, BaseMessage message) {
        logging(message);
        ProducerRecord<String, BaseMessage> record = getRecord(topic, message);
        kafkaTemplate.send(record);
    }

    @Override
    public void sendWithWait(String topic, BaseMessage message) {
        kafkaRetryTemplate.execute(context -> {
            try {
                logging(message);

                ProducerRecord<String, BaseMessage> record =
                        getRecord(topic, message);
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

    private ProducerRecord<String, BaseMessage> getRecord(String topic, BaseMessage message) {
        var record = new ProducerRecord<>(topic, message.getTraceId(), message);
        Map<String, String> headers = Map.of(
                "outgoing_topic", kafkaProperties.getTopic(),
                "source-service", "order-service",
                "timestamp", String.valueOf(System.currentTimeMillis())
        );
        headers.forEach((key, value) ->
                record.headers().add(key, value.getBytes(StandardCharsets.UTF_8))
        );
        return record;
    }

    private static void logging(BaseMessage message) {
        log.info("Response sent to Kafka: traceId={}, orderId={}, productId={}, status={}, timestamp={}",
                message.getTraceId(), message.getOrderId(), message.getProductId(),
                message.getStatus(), message.getTimestamp());
    }
}

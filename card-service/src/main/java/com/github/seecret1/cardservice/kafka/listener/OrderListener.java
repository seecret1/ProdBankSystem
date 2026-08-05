package com.github.seecret1.cardservice.kafka.listener;

import com.github.seecret1.cardservice.kafka.service.ProducerSenderDlt;
import com.github.seecret1.cardservice.dto.order.OrderStatus;
import com.github.seecret1.cardservice.dto.order.message.BaseMessage;
import com.github.seecret1.cardservice.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderListener {

    private final CardService service;

    private final ProducerSenderDlt kafkaSenderDlt;

    @KafkaListener(
            topics = "${app.kafka.response-topic}",
            groupId = "${app.kafka.groupId}",
            containerFactory = "responseCardKafkaListenerContainerFactory"
    )
    public void listenOrderCardResponses(
            @Payload BaseMessage order,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) UUID key,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(value = KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp
    ) {
        log.info("Response order received: traceId={} orderId={}, userId={}, cardData={}, createdAt={}",
                order.getTraceId(), order.getOrderId(), order.getUserId(), order.getProductId(), order.getTimestamp());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                key, partition, topic, Instant.ofEpochMilli(timestamp));

        try {
            orderProcessing(order);
        } catch (Exception e) {
            log.error("Error processing order: traceId={}, orderId={}, error={}",
                    order.getTraceId(), order.getOrderId(), e.getMessage(), e);
            kafkaSenderDlt.sendMessageInDlt(order);
        }

        log.debug("Response order body: {}", order);
    }

    public void orderProcessing(BaseMessage order) {
        String cardId = order.getProductId();
        OrderStatus status = order.getStatus();

        log.info("Processing order with status: {} for card: {}", status, cardId);

        switch (status) {
            case SUCCESS:
                service.activateCard(cardId);
                sendNotification();
                break;

            case PENDING:
                // TODO: Добавить работу с retry топиком
                sendNotification();
                break;

            case REJECTED:
                sendNotification();
                break;

            case ERROR:
                kafkaSenderDlt.sendMessageInDlt(order);
                sendNotification();
                break;

            default:
                log.warn("Unknown order status: {} for order: {}", status, order.getOrderId());
                break;
        }
    }

    private void sendNotification() {
        //TODO: Добавить обработку 1, затем заменить на 2:
        // 1) Отправить сообщение по email (SMTP)
        // 2) Добавить работу с notification-service
        log.info("Send message to person");
    }
}

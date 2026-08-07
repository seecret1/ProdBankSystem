package com.github.seecret1.delivery_service.kafka.listener;

import com.github.seecret1.delivery_service.dto.order.OrderDeliveryDto;
import com.github.seecret1.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryRetryListener {

    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "${app.kafka.retry-topic}",
            groupId = "${app.kafka.retry-group-id}",
            containerFactory = "retryKafkaListenerContainerFactory"
    )
    public void listenRetry(
            @Payload OrderDeliveryDto event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "retry-count", required = false) Integer retryCount
    ) {
        int currentRetry = retryCount != null ? retryCount : 1;
        log.info("Retry delivery received traceId={}, retryCount={}, originalTopic={}",
                event.getTraceId(), currentRetry, topic);
        try {
            deliveryService.create(event);
            log.info("Retry processed successfully traceId={}", event.getTraceId());
        } catch (Exception ex) {
            log.error("Retry processing failed for traceId={}", event.getTraceId(), ex);
            throw ex;
        }
    }
}

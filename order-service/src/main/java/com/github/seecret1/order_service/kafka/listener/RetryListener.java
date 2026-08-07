package com.github.seecret1.order_service.kafka.listener;

import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.service.OrderCardService;
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
public class RetryListener {

    private final OrderCardService orderCardService;

    @KafkaListener(
            topics = "${app.kafka.retry-topic}",
            groupId = "${app.kafka.retry-group-id}",
            containerFactory = "retryOrderKafkaListenerContainerFactory"
    )
    public void listenRetry(
            @Payload OrderCardDto event,
            @Header(value = KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(value = "retry-count", required = false) Integer retryCount
    ) {
        int currentRetry = retryCount != null ? retryCount : 1;
        log.info("Retry delivery received traceId={}, retryCount={}, originalTopic={}",
                event.getTraceId(), currentRetry, topic);
        try {
            orderCardService.createOrder(event);
            log.info("Retry processed successfully traceId={}", event.getTraceId());
        } catch (Exception ex) {
            log.error("Retry processing failed for traceId={}", event.getTraceId(), ex);
            throw ex;
        }
    }
}

package com.github.seecret1.order_service.kafka.listener;

import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.service.OrderCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryConsumer {

    private final OrderCardService orderCardService;

    @KafkaListener(
            topics = "${app.kafka.retry-topic}",
            groupId = "${app.kafka.retry-group-id}",
            containerFactory = "retryOrderKafkaListenerContainerFactory"
    )
    public void listenRetry(
            ConsumerRecord<String, OrderCardDto> event,
            @Header(value = "retry-count", required = false) Integer retryCount
    ) {
        int currentRetry = retryCount != null ? retryCount : 1; //TODO: проверить работу без @Header
        log.info("Retry delivery received traceId={}, retryCount={}, originalTopic={}",
                event.value().getTraceId(), event.headers().headers("retry-count"), event.topic());
        try {
            orderCardService.createOrder(event.value());
            log.info("Retry processed successfully traceId={}", event.value().getTraceId());
        } catch (Exception ex) {
            log.error("Retry processing failed for traceId={}", event.value().getTraceId(), ex);
            throw ex;
        }
    }
}

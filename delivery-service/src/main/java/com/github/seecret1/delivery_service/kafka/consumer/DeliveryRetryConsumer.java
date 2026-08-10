package com.github.seecret1.delivery_service.kafka.consumer;

import com.github.seecret1.delivery_service.dto.order.OrderDeliveryDto;
import com.github.seecret1.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryRetryConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "${app.kafka.retry-topic}",
            groupId = "${app.kafka.retry-group-id}",
            containerFactory = "retryKafkaListenerContainerFactory"
    )
    public void listenRetry(
            ConsumerRecord<String, OrderDeliveryDto> event,
            @Header(value = "retry-count", required = false) Integer retryCount
    ) {
        OrderDeliveryDto dto = event.value();
        int currentRetry = retryCount != null ? retryCount : 1; //TODO: проверить работу без @Header
        log.info("Retry delivery received traceId={}, retryCount={}, originalTopic={}",
                dto.getTraceId(), event.headers().headers("retry-count"), event.topic());
        try {
            deliveryService.create(dto);
            log.debug("Retry processed successfully traceId={}", dto.getTraceId());
        } catch (Exception ex) {
            log.error("Retry processing failed for traceId={}", dto.getTraceId(), ex);
            throw ex;
        }
    }
}

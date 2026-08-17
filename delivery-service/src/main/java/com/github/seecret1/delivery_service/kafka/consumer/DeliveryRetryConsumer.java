package com.github.seecret1.delivery_service.kafka.consumer;

import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;
import com.github.seecret1.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
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
            ConsumerRecord<String, OrderCardDeliveryDto> event
    ) {
        OrderCardDeliveryDto dto = event.value();
        log.info("Retry delivery received: traceId={}, topic={}, partition={}, offset={}",
                dto.getTraceId(), event.topic(), event.partition(), event.offset());
        try {
            deliveryService.create(dto);
            log.info("Retry processed successfully: traceId={}", dto.getTraceId());
        } catch (Exception ex) {
            log.error("Retry processing failed for traceId={}, will be sent to DLT", dto.getTraceId(), ex);
            throw ex;
        }
    }
}

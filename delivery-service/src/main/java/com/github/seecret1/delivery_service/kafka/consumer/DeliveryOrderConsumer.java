package com.github.seecret1.delivery_service.kafka.consumer;

import com.github.seecret1.delivery_service.dto.order.OrderDeliveryDto;
import com.github.seecret1.delivery_service.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryOrderConsumer {

    private final DeliveryService deliveryService;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "deliveryKafkaListenerContainerFactory"
    )
    public void listenDeliveryOrder(
            ConsumerRecord<String, OrderDeliveryDto> event
    ) {
        OrderDeliveryDto dto = event.value();
        log.info("Order delivery received: traceId={}, userId={}, originAddress={}, destinationAddress={}, createdAt={}",
                dto.getTraceId(), dto.getUserId(), dto.getOriginAddress(), dto.getDestinationAddress(), dto.getCreatedAt());
        log.debug("Message details: partition={}, offset={}, timestamp={}", 
                event.partition(), event.offset(), event.timestamp());
        try {
            deliveryService.create(dto);
            log.info("Order delivery processed successfully: traceId={}", dto.getTraceId());
        } catch (Exception ex) {
            log.error("Order delivery processing failed: traceId={}, sending to retry topic", dto.getTraceId(), ex);
            throw ex;
        }
    }
}

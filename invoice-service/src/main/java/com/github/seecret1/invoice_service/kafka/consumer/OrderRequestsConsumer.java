package com.github.seecret1.invoice_service.kafka.consumer;

import com.github.seecret1.invoice_service.dto.order.OrderCardDto;
import com.github.seecret1.invoice_service.service.process.InvoiceProcessed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRequestsConsumer {

    private final InvoiceProcessed invoiceProcessed;

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "orderRequestKafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, OrderCardDto> record) {
        OrderCardDto dto = record.value();
        log.info("Order received: traceId={}, userId={}, invoiceId={}",
                dto.getTraceId(), dto.getUserId(), dto.getInvoiceId());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                record.key(), record.partition(), record.topic(), Instant.ofEpochMilli(record.timestamp()));

        try {
            invoiceProcessed.processOrder(dto);
            log.debug("[topic: {}][traceId: {}]Processing card order successfully", record.topic(), dto.getTraceId());
        } catch (Exception ex) {
            log.error("Error while creating order. Send to retry topic", ex);
            throw ex;
        }
    }
}

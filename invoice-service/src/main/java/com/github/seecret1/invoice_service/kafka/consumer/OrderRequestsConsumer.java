package com.github.seecret1.invoice_service.kafka.consumer;

import com.github.seecret1.invoice_service.dto.order.OrderInvoiceDto;
import com.github.seecret1.invoice_service.service.process.InvoiceProcessing;
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

    private final InvoiceProcessing invoiceProcessingImpl;

    @KafkaListener(
            topics = "${app.kafka.request-topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "orderRequestKafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, OrderInvoiceDto> record) {
        OrderInvoiceDto dto = record.value();
        log.info("Order received: traceId={}, userId={}, cardId={}, orderId={}, orderType={}, cardType={}",
                dto.getTraceId(), dto.getUserId(), dto.getCardId(), dto.getOrderId(),
                dto.getOrderType(), dto.getCardType());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                record.key(), record.partition(), record.topic(), Instant.ofEpochMilli(record.timestamp()));

        try {
            invoiceProcessingImpl.processOrder(dto);
            log.debug("[topic: {}][traceId: {}]Processing card order successfully", record.topic(), dto.getTraceId());
        } catch (Exception ex) {
            log.error("Error while creating order. Send to retry topic", ex);
            throw ex;
        }
    }
}

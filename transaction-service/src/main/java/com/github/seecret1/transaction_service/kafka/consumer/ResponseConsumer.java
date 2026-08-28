package com.github.seecret1.transaction_service.kafka.consumer;

import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponseConsumer {

    private final TransactionService transactionService;

    @KafkaListener(
            topics = "${app.kafka.request-topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "orderRequestKafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, TransactionMessage> record) {
        TransactionMessage dto = record.value();
        log.info("Order received: traceId={}, userId={}, sourceInvoiceId={}, destinationInvoiceId={}, paymentType={}",
                dto.getTraceId(), dto.getUserId(), dto.getSourceInvoiceId(), dto.getDestinationInvoiceId(),
                dto.getPaymentType());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                record.key(), record.partition(), record.topic(), Instant.ofEpochMilli(record.timestamp()));

        try {
            transactionService.process(dto);
            log.debug("[topic: {}][traceId: {}]Processing card order successfully", record.topic(), dto.getTraceId());
        } catch (Exception ex) {
            log.error("Error while creating order. Send to retry topic", ex);
            throw ex;
        }
    }
}

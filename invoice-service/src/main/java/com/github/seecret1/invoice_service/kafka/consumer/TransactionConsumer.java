package com.github.seecret1.invoice_service.kafka.consumer;

import com.github.seecret1.invoice_service.dto.message.TransactionMessage;
import com.github.seecret1.invoice_service.service.process.TransactionProcessing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumer {

    private final TransactionProcessing transactionProcessing;

    @KafkaListener(
            topics = "${app.kafka.invoice-transaction-topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "transactionKafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, TransactionMessage> record) {
        TransactionMessage message = record.value();
        log.info("Transaction received: traceId={}, userId={}, sourceInvoiceId={}, destinationInvoiceId={}, paymentType={}, currency={}",
                message.getTraceId(), message.getUserId(), message.getSourceInvoiceId(),
                message.getDestinationInvoiceId(), message.getPaymentType(), message.getCurrency());
        log.info("Key: {}; Partition: {}; Topic: {}; Offset: {} Timestamp: {}",
                record.key(), record.partition(), record.topic(), record.offset(), Instant.ofEpochMilli(record.timestamp()));

        try {
            transactionProcessing.transactionProcessing(message);
            log.debug("[topic: {}][traceId: {}]Transaction processing", record.topic(), message.getTraceId());
        } catch (Exception ex) {
            log.error("Error processing transaction. Send to retry topic", ex);
            throw ex;
        }
    }
}

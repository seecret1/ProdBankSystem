package com.github.seecret1.payment_service.kafka.consumer;

import com.github.seecret1.payment_service.dto.message.TransactionMessage;
import com.github.seecret1.payment_service.service.processing.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResponsePaymentConsumer {

    private final PaymentProcessingService paymentProcessingService;

    @KafkaListener(
            topics = "${app.kafka.response-topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenBaseMessageInResponseTopic(
            ConsumerRecord<String, TransactionMessage> record
    ) {
        TransactionMessage message = record.value();
        log.info("Order received: traceId={}, userId={}, sourceInvoiceId={}, data={}, timestamp={}",
                message.getTraceId(), message.getUserId(), message.getSourceInvoiceId(),
                message.getData(), message.getTimestamp());
        log.info("Key: {}; Partition: {}; Topic: {}; Timestamp: {}",
                record.key(), record.partition(), record.topic(), Instant.ofEpochMilli(record.timestamp()));

        try {
            paymentProcessingService.processMessage(message);
            log.debug("[topic: {}][traceId: {}]Processing card order successfully", record.topic(), message.getTraceId());
        } catch (Exception ex) {
            log.error("Error while creating order. Send to retry topic", ex);
            throw ex;
        }
    }
}

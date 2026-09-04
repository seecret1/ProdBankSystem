package com.github.seecret1.payment_service.kafka.consumer;

import com.github.seecret1.payment_service.dto.message.TransactionMessage;
import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import com.github.seecret1.payment_service.service.processing.PaymentProcessingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ResponsePaymentConsumer Unit Tests")
class ResponsePaymentConsumerTest {

    @Mock
    private PaymentProcessingService paymentProcessingService;

    @InjectMocks
    private ResponsePaymentConsumer consumer;

    @Test
    @DisplayName("listenBaseMessageInResponseTopic: should delegate to processing service")
    void shouldDelegateToProcessingService() {
        var message = TransactionMessage.builder()
                .traceId(UUID.randomUUID().toString())
                .userId("user-1")
                .sourceInvoiceId("src-1")
                .destinationInvoiceId("dst-1")
                .amount(new BigDecimal("100.00"))
                .currency("RUB")
                .paymentType(PaymentType.TRANSFER)
                .status(PaymentStatus.COMPLETED)
                .build();
        var record = new ConsumerRecord<>("payment-response", 0, 0L, "key", message);

        consumer.listenBaseMessageInResponseTopic(record);

        verify(paymentProcessingService).processMessage(message);
    }

    @Test
    @DisplayName("listen: should rethrow exception from processing service")
    void shouldRethrowOnError() {
        var message = TransactionMessage.builder().traceId("t").userId("u").build();
        var record = new ConsumerRecord<>("payment-response", 0, 0L, "key", message);
        doThrow(new RuntimeException("processing failed")).when(paymentProcessingService).processMessage(message);

        assertThatThrownBy(() -> consumer.listenBaseMessageInResponseTopic(record))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("processing failed");
    }

    @Test
    @DisplayName("listen: should handle message with null data")
    void shouldHandleNullData() {
        var message = new TransactionMessage();
        message.setTraceId("trace-null-data");
        message.setUserId("user-null");
        var record = new ConsumerRecord<>("payment-response", 1, 0L, "k", message);

        consumer.listenBaseMessageInResponseTopic(record);

        verify(paymentProcessingService).processMessage(message);
    }
}

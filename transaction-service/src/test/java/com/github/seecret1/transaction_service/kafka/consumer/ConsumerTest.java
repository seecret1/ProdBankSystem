package com.github.seecret1.transaction_service.kafka.consumer;

import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.entity.enums.PaymentStatus;
import com.github.seecret1.transaction_service.entity.enums.PaymentType;
import com.github.seecret1.transaction_service.service.TransactionService;
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
@DisplayName("Transaction Consumers Unit Tests")
class ConsumerTest {

    @Mock private TransactionService transactionService;

    @Test @DisplayName("RequestsConsumer: should delegate to processRequest")
    void shouldDelegateRequestsConsumer() {
        RequestsConsumer consumer = new RequestsConsumer(transactionService);
        var msg = TransactionMessage.builder().traceId(UUID.randomUUID().toString()).userId("u1").paymentType(PaymentType.TRANSFER).amount(new BigDecimal("10.00")).currency("RUB").status(PaymentStatus.CREATED).build();
        var rec = new ConsumerRecord<>("transaction-request", 0, 0L, "k", msg);
        consumer.listen(rec);
        verify(transactionService).processRequest(msg);
    }

    @Test @DisplayName("ResponseConsumer: should delegate to processResponse")
    void shouldDelegateResponseConsumer() {
        ResponseConsumer consumer = new ResponseConsumer(transactionService);
        var msg = TransactionMessage.builder().traceId(UUID.randomUUID().toString()).userId("u1").paymentType(PaymentType.TRANSFER).status(PaymentStatus.COMPLETED).build();
        var rec = new ConsumerRecord<>("transaction-response", 0, 0L, "k", msg);
        consumer.listen(rec);
        verify(transactionService).processResponse(msg);
    }

    @Test @DisplayName("RequestsConsumer: should rethrow on service exception")
    void shouldRethrowRequests() {
        RequestsConsumer consumer = new RequestsConsumer(transactionService);
        var msg = TransactionMessage.builder().traceId("t").build();
        var rec = new ConsumerRecord<>("transaction-request", 0, 0L, "k", msg);
        doThrow(new RuntimeException("fail")).when(transactionService).processRequest(msg);
        assertThatThrownBy(() -> consumer.listen(rec)).isInstanceOf(RuntimeException.class);
    }

    @Test @DisplayName("ResponseConsumer: should rethrow on service exception")
    void shouldRethrowResponse() {
        ResponseConsumer consumer = new ResponseConsumer(transactionService);
        var msg = TransactionMessage.builder().traceId("t").build();
        var rec = new ConsumerRecord<>("transaction-response", 0, 0L, "k", msg);
        doThrow(new RuntimeException("fail")).when(transactionService).processResponse(msg);
        assertThatThrownBy(() -> consumer.listen(rec)).isInstanceOf(RuntimeException.class);
    }
}

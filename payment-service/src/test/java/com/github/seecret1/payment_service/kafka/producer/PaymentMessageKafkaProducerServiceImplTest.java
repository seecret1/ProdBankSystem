package com.github.seecret1.payment_service.kafka.producer;

import com.github.seecret1.payment_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.payment_service.dto.message.TransactionMessage;
import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import com.github.seecret1.payment_service.kafka.producer.impl.PaymentMessageKafkaProducerServiceImpl;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.support.RetryTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentMessageKafkaProducerServiceImpl Unit Tests")
class PaymentMessageKafkaProducerServiceImplTest {

    @Mock
    private KafkaProperties kafkaProperties;

    @Mock
    private RetryTemplate kafkaRetryTemplate;

    @Mock
    private KafkaTemplate<String, TransactionMessage> kafkaTemplate;

    private PaymentMessageKafkaProducerServiceImpl producerService;

    private TransactionMessage message;

    @BeforeEach
    void setUp() {
        producerService = new PaymentMessageKafkaProducerServiceImpl(kafkaProperties, kafkaRetryTemplate, kafkaTemplate);
        message = TransactionMessage.builder()
                .traceId(UUID.randomUUID().toString())
                .userId("user-1")
                .sourceInvoiceId("src-1")
                .destinationInvoiceId("dst-1")
                .amount(new BigDecimal("100.00"))
                .currency("RUB")
                .paymentType(PaymentType.TRANSFER)
                .status(PaymentStatus.CREATED)
                .build();
        lenient().when(kafkaProperties.getTransactionTopic()).thenReturn("transaction-request");
        lenient().when(kafkaProperties.getTopic()).thenReturn("payment");
        lenient().when(kafkaProperties.getTimeoutSeconds()).thenReturn(5);
    }

    @Test
    @DisplayName("sendNoWait: should send record with traceId as key")
    @SuppressWarnings("unchecked")
    void shouldSendNoWait() {
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        producerService.sendNoWait(message);

        ArgumentCaptor<ProducerRecord<String, TransactionMessage>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        ProducerRecord<String, TransactionMessage> record = captor.getValue();
        assertThat(record.key()).isEqualTo(message.getTraceId());
        assertThat(record.topic()).isEqualTo("transaction-request");
        assertThat(record.value()).isEqualTo(message);
        assertThat(record.headers().lastHeader("source-service")).isNotNull();
    }

    @Test
    @DisplayName("sendWithWait: should delegate to RetryTemplate")
    void shouldSendWithWait() {
        when(kafkaRetryTemplate.execute(any())).thenAnswer(inv -> {
            // simulate retry template calling the callback
            var callback = (org.springframework.retry.RetryCallback<Object, RuntimeException>) inv.getArgument(0);
            return callback.doWithRetry(null);
        });
        CompletableFuture future = CompletableFuture.completedFuture(mock(SendResult.class));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        assertThatCode(() -> producerService.sendWithWait(message))
                .doesNotThrowAnyException();

        verify(kafkaRetryTemplate).execute(any());
        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    @Test
    @DisplayName("sendNoWait: should handle null traceId")
    @SuppressWarnings("unchecked")
    void shouldHandleNullTraceId() {
        message.setTraceId(null);
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        producerService.sendNoWait(message);

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }
}

package com.github.seecret1.transaction_service.kafka.producer;

import com.github.seecret1.transaction_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.entity.enums.PaymentStatus;
import com.github.seecret1.transaction_service.entity.enums.PaymentType;
import com.github.seecret1.transaction_service.kafka.producer.impl.TransactionMessageKafkaProducerServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionMessageKafkaProducerServiceImpl Unit Tests")
class TransactionMessageKafkaProducerServiceImplTest {

    @Mock private KafkaProperties kafkaProperties;
    @Mock private RetryTemplate retryTemplate;
    @Mock private KafkaTemplate<String, TransactionMessage> kafkaTemplate;

    private TransactionMessageKafkaProducerServiceImpl service;
    private TransactionMessage msg;

    @BeforeEach
    void setUp() {
        service = new TransactionMessageKafkaProducerServiceImpl(kafkaProperties, retryTemplate, kafkaTemplate);
        msg = TransactionMessage.builder().traceId(UUID.randomUUID().toString()).userId("u1").amount(new BigDecimal("10.00")).currency("RUB").paymentType(PaymentType.TRANSFER).status(PaymentStatus.CREATED).build();
        lenient().when(kafkaProperties.getTimeoutSeconds()).thenReturn(5);
    }

    @Test @DisplayName("sendNoWait: should send to given topic")
    void shouldSendNoWait() {
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        service.sendNoWait("invoice-topic", msg);
        ArgumentCaptor<ProducerRecord<String, TransactionMessage>> cap = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(cap.capture());
        assertThat(cap.getValue().topic()).isEqualTo("invoice-topic");
        assertThat(cap.getValue().key()).isEqualTo(msg.getTraceId());
    }

    @Test @DisplayName("sendWithWait: should use retryTemplate")
    void shouldSendWithWait() {
        when(retryTemplate.execute(any())).thenAnswer(inv -> {
            var cb = (org.springframework.retry.RetryCallback<Object, RuntimeException>) inv.getArgument(0);
            return cb.doWithRetry(null);
        });
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        service.sendWithWait("payment-topic", msg);
        verify(retryTemplate).execute(any());
        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }
}

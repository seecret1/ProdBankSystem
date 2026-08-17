package com.github.seecret1.delivery_service.kafka.producer;

import com.github.seecret1.delivery_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.entity.enums.OrderStatus;
import com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.seecret1.delivery_service.utils.DeliveryTestDataFactory.TRACE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryKafkaProducerService Unit Tests")
class DeliveryKafkaProducerServiceImplTest {

    private static final String ORDERS_TOPIC = "orders-topic";

    @Mock
    private KafkaProperties kafkaProperties;

    @Mock
    private RetryTemplate kafkaRetryTemplate;

    @Mock
    private KafkaTemplate<String, BaseMessage> kafkaTemplate;

    @InjectMocks
    private DeliveryKafkaProducerServiceImpl kafkaProducerService;

    private BaseMessage message;

    @BeforeEach
    void setUp() {
        message = BaseMessage.builder()
                .traceId(TRACE_ID)
                .orderId(DeliveryTestDataFactory.ORDER_ID)
                .userId(DeliveryTestDataFactory.USER_ID)
                .status(OrderStatus.SUCCESS)
                .build();
        when(kafkaProperties.getOrdersTopic()).thenReturn(ORDERS_TOPIC);
    }

    @Test
    @DisplayName("Should send message without waiting")
    void shouldSendMessageNoWait() {
        kafkaProducerService.sendNoWait(message);

        ArgumentCaptor<ProducerRecord<String, BaseMessage>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, BaseMessage> record = captor.getValue();
        assertThat(record.topic()).isEqualTo(ORDERS_TOPIC);
        assertThat(record.key()).isEqualTo(TRACE_ID);
        assertThat(record.value()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should send message with wait successfully")
    void shouldSendMessageWithWaitSuccessfully() throws Exception {
        when(kafkaRetryTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback<?, ?> callback = invocation.getArgument(0);
            return callback.doWithRetry(null);
        });
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(new SendResult<>(null, null)));

        kafkaProducerService.sendWithWait(message);

        ArgumentCaptor<ProducerRecord<String, BaseMessage>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertThat(captor.getValue().topic()).isEqualTo(ORDERS_TOPIC);
        assertThat(captor.getValue().key()).isEqualTo(TRACE_ID);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when interrupted during send with wait")
    void shouldThrowWhenInterruptedDuringSendWithWait() throws Exception {
        when(kafkaRetryTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback<?, ?> callback = invocation.getArgument(0);
            return callback.doWithRetry(null);
        });
        CompletableFuture<SendResult<String, BaseMessage>> future = new CompletableFuture<>();
        future.completeExceptionally(new InterruptedException("interrupted"));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        assertThatThrownBy(() -> kafkaProducerService.sendWithWait(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to send message to Kafka");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when execution fails during send with wait")
    void shouldThrowWhenExecutionFailsDuringSendWithWait() throws Exception {
        when(kafkaRetryTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback<?, ?> callback = invocation.getArgument(0);
            return callback.doWithRetry(null);
        });
        CompletableFuture<SendResult<String, BaseMessage>> future = new CompletableFuture<>();
        future.completeExceptionally(new ExecutionException(new RuntimeException("kafka down")));
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        assertThatThrownBy(() -> kafkaProducerService.sendWithWait(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to send message to Kafka");
    }

    @Test
    @DisplayName("Should use traceId as Kafka message key")
    void shouldUseTraceIdAsKafkaKey() {
        kafkaProducerService.sendNoWait(message);

        ArgumentCaptor<ProducerRecord<String, BaseMessage>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo(TRACE_ID);
    }
}

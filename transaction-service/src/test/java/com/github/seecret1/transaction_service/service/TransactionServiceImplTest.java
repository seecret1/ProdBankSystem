package com.github.seecret1.transaction_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.transaction_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.dto.payment.PaymentResponse;
import com.github.seecret1.transaction_service.dto.transaction.TransactionDto;
import com.github.seecret1.transaction_service.entity.Transaction;
import com.github.seecret1.transaction_service.entity.enums.PaymentStatus;
import com.github.seecret1.transaction_service.entity.enums.PaymentType;
import com.github.seecret1.transaction_service.entity.enums.TransactionStatus;
import com.github.seecret1.transaction_service.entity.enums.TransactionType;
import com.github.seecret1.transaction_service.kafka.producer.TransactionMessageKafkaProducerService;
import com.github.seecret1.transaction_service.mapper.TransactionMapper;
import com.github.seecret1.transaction_service.repository.TransactionRepository;
import com.github.seecret1.transaction_service.service.impl.TransactionServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl Unit Tests")
class TransactionServiceImplTest {

    @Mock
    private TransactionMessageKafkaProducerService producerService;
    @Mock
    private KafkaProperties kafkaProperties;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TransactionServiceImpl service;

    private TransactionMessage requestMessage;
    private PaymentResponse paymentResponse;
    private Transaction transaction;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        paymentResponse = new PaymentResponse("pay-123", "src-1", "dst-1", new BigDecimal("100.00"), PaymentType.TRANSFER, "RUB");
        requestMessage = TransactionMessage.builder()
                .traceId(UUID.randomUUID().toString())
                .userId("user-1")
                .sourceInvoiceId("src-1")
                .destinationInvoiceId("dst-1")
                .amount(new BigDecimal("100.00"))
                .currency("RUB")
                .paymentType(PaymentType.TRANSFER)
                .status(PaymentStatus.CREATED)
                .data(paymentResponse)
                .build();
        transaction = Transaction.builder()
                .id("txn-1")
                .paymentId("pay-123")
                .userId("user-1")
                .sourceInvoiceId("src-1")
                .destinationInvoiceId("dst-1")
                .amount(new BigDecimal("100.00"))
                .currency("RUB")
                .transactionType(TransactionType.CREDIT)
                .status(TransactionStatus.PENDING)
                .build();
        transactionDto = new TransactionDto("txn-1", "user-1", "pay-123", "src-1", "dst-1", new BigDecimal("100.00"), "RUB", TransactionType.CREDIT, TransactionStatus.PENDING);
        lenient().when(kafkaProperties.getInvoiceTopic()).thenReturn("invoice-transaction");
        lenient().when(kafkaProperties.getPaymentTopic()).thenReturn("payment-response");
    }

    @Test
    @DisplayName("processRequest: should save transaction, send to invoice, then set PROCESSING")
    void shouldProcessRequest() {
        when(objectMapper.convertValue(requestMessage.getData(), PaymentResponse.class)).thenReturn(paymentResponse);
        when(transactionMapper.toEntity(requestMessage, paymentResponse.id())).thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.toDto(transaction)).thenReturn(transactionDto);

        service.processRequest(requestMessage);

        assertThat(requestMessage.getData()).isEqualTo(transactionDto);
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(producerService).sendWithWait(eq("invoice-transaction"), any(TransactionMessage.class));
        // second save should have status PROCESSING
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(captor.capture());
        Transaction secondSaved = captor.getAllValues().get(1);
        assertThat(secondSaved.getStatus()).isEqualTo(TransactionStatus.PROCESSING);
    }

    @Test
    @DisplayName("processRequest: should propagate exception when mapping fails")
    void shouldThrowWhenMappingFails() {
        when(objectMapper.convertValue(any(), eq(PaymentResponse.class))).thenThrow(new IllegalArgumentException("bad data"));
        assertThatThrownBy(() -> service.processRequest(requestMessage))
                .isInstanceOf(IllegalArgumentException.class);
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("processResponse: should update status based on PaymentStatus COMPLETED")
    void shouldProcessResponseCompleted() {
        TransactionMessage responseMessage = TransactionMessage.builder()
                .traceId(UUID.randomUUID().toString())
                .userId("user-1")
                .status(PaymentStatus.COMPLETED)
                .data(transactionDto)
                .build();
        when(objectMapper.convertValue(responseMessage.getData(), TransactionDto.class)).thenReturn(transactionDto);
        when(transactionRepository.findById(transactionDto.id())).thenReturn(Optional.of(transaction));

        service.processResponse(responseMessage);

        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        verify(transactionRepository).save(transaction);
        verify(producerService).sendWithWait(eq("payment-response"), eq(responseMessage));
    }

    @Test
    @DisplayName("processResponse: should map REJECTED to FAILED")
    void shouldMapRejectedToFailed() {
        TransactionMessage msg = TransactionMessage.builder().status(PaymentStatus.REJECTED).data(transactionDto).build();
        when(objectMapper.convertValue(msg.getData(), TransactionDto.class)).thenReturn(transactionDto);
        when(transactionRepository.findById(transactionDto.id())).thenReturn(Optional.of(transaction));

        service.processResponse(msg);

        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    @DisplayName("processResponse: should throw EntityNotFound when transaction not found")
    void shouldThrowWhenTransactionNotFound() {
        TransactionMessage msg = TransactionMessage.builder().status(PaymentStatus.COMPLETED).data(transactionDto).build();
        when(objectMapper.convertValue(msg.getData(), TransactionDto.class)).thenReturn(transactionDto);
        when(transactionRepository.findById(transactionDto.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processResponse(msg))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Not found transaction by ID: " + transactionDto.id());
    }

    @Test
    @DisplayName("processResponse: should handle all PaymentStatus mappings")
    void shouldHandleAllStatusMappings() {
        for (PaymentStatus ps : PaymentStatus.values()) {
            TransactionMessage msg = TransactionMessage.builder().status(ps).data(transactionDto).build();
            when(objectMapper.convertValue(msg.getData(), TransactionDto.class)).thenReturn(transactionDto);
            // need fresh transaction each iteration
            Transaction fresh = Transaction.builder().id("txn-1").paymentId("pay-123").status(TransactionStatus.PENDING).build();
            when(transactionRepository.findById(transactionDto.id())).thenReturn(Optional.of(fresh));

            service.processResponse(msg);
            assertThat(fresh.getStatus()).isNotNull();
            verify(transactionRepository, atLeastOnce()).save(fresh);
            verify(producerService, atLeastOnce()).sendWithWait(anyString(), any());
        }
    }
}

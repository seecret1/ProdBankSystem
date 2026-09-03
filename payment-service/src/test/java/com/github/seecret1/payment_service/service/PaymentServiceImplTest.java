package com.github.seecret1.payment_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.payment_service.dto.message.TransactionMessage;
import com.github.seecret1.payment_service.dto.payment.PaymentRequest;
import com.github.seecret1.payment_service.dto.payment.PaymentResponse;
import com.github.seecret1.payment_service.dto.transaction.TransactionDto;
import com.github.seecret1.payment_service.entity.Payment;
import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import com.github.seecret1.payment_service.entity.enums.TransactionStatus;
import com.github.seecret1.payment_service.entity.enums.TransactionType;
import com.github.seecret1.payment_service.kafka.producer.PaymentMessageKafkaProducerService;
import com.github.seecret1.payment_service.mapper.PaymentManualMapper;
import com.github.seecret1.payment_service.repository.PaymentRepository;
import com.github.seecret1.payment_service.service.impl.PaymentServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentServiceImpl Unit Tests")
class PaymentServiceImplTest {

    @Mock
    private PaymentMessageKafkaProducerService producerService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentManualMapper paymentMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private final String userId = "user-123";
    private PaymentRequest request;
    private Payment payment;
    private PaymentResponse paymentResponse;
    private TransactionMessage transactionMessage;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        request = new PaymentRequest("src-inv-1", "dst-inv-1", new BigDecimal("100.00"), PaymentType.TRANSFER, "RUB");

        payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .sourceInvoiceId("src-inv-1")
                .destinationInvoiceId("dst-inv-1")
                .amount(new BigDecimal("100.00"))
                .currency("RUB")
                .paymentType(PaymentType.TRANSFER)
                .status(PaymentStatus.CREATED)
                .build();

        paymentResponse = new PaymentResponse(payment.getId(), "src-inv-1", "dst-inv-1", new BigDecimal("100.00"), PaymentType.TRANSFER, "RUB");

        transactionDto = new TransactionDto("txn-1", userId, payment.getId(), "src-inv-1", "dst-inv-1", new BigDecimal("100.00"), "RUB", TransactionType.CREDIT, TransactionStatus.COMPLETED);

        transactionMessage = TransactionMessage.builder()
                .traceId(UUID.randomUUID().toString())
                .userId(userId)
                .sourceInvoiceId("src-inv-1")
                .destinationInvoiceId("dst-inv-1")
                .amount(new BigDecimal("100.00"))
                .currency("RUB")
                .paymentType(PaymentType.TRANSFER)
                .status(PaymentStatus.COMPLETED)
                .data(transactionDto)
                .build();
    }

    @Test
    @DisplayName("create: should save payment with CREATED status and send kafka message")
    void shouldCreatePayment() {
        when(paymentMapper.toPayment(userId, request, PaymentStatus.CREATED)).thenReturn(payment);
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(paymentResponse);
        var txMsg = TransactionMessage.builder().traceId("t").userId(userId).build();
        when(paymentMapper.toTransactionMessage(payment, paymentResponse)).thenReturn(txMsg);
        when(paymentRepository.save(payment)).thenReturn(payment);

        PaymentResponse result = paymentService.create(userId, request);

        assertThat(result).isEqualTo(paymentResponse);
        verify(paymentRepository).save(payment);
        verify(producerService).sendWithWait(txMsg);
        verify(paymentMapper).toPayment(userId, request, PaymentStatus.CREATED);
    }

    @Test
    @DisplayName("create: should propagate exception when kafka send fails")
    void shouldThrowWhenKafkaSendFails() {
        when(paymentMapper.toPayment(any(), any(), any())).thenReturn(payment);
        when(paymentMapper.toPaymentResponse(any())).thenReturn(paymentResponse);
        when(paymentMapper.toTransactionMessage(any(), any())).thenReturn(transactionMessage);
        doThrow(new RuntimeException("kafka down")).when(producerService).sendWithWait(any());

        assertThatThrownBy(() -> paymentService.create(userId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("kafka down");

        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("update: should find payment and return response")
    void shouldUpdatePayment() {
        when(objectMapper.convertValue(transactionMessage.getData(), TransactionDto.class)).thenReturn(transactionDto);
        when(paymentRepository.findById(transactionDto.paymentId())).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.update(transactionMessage);

        assertThat(result).isEqualTo(paymentResponse);
        verify(paymentRepository).findById(transactionDto.paymentId());
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("update: should throw EntityNotFoundException when payment not found")
    void shouldThrowWhenPaymentNotFoundOnUpdate() {
        when(objectMapper.convertValue(any(), eq(TransactionDto.class))).thenReturn(transactionDto);
        when(paymentRepository.findById(transactionDto.paymentId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.update(transactionMessage))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Not found payment by ID: " + transactionDto.paymentId());
    }

    @Test
    @DisplayName("create: should handle different PaymentTypes")
    void shouldCreateWithDifferentPaymentTypes() {
        for (PaymentType type : PaymentType.values()) {
            PaymentRequest req = new PaymentRequest("src", "dst", new BigDecimal("10.00"), type, "USD");
            Payment mapped = Payment.builder().userId(userId).sourceInvoiceId("src").destinationInvoiceId("dst").amount(new BigDecimal("10.00")).currency("USD").paymentType(type).status(PaymentStatus.CREATED).build();
            when(paymentMapper.toPayment(userId, req, PaymentStatus.CREATED)).thenReturn(mapped);
            when(paymentMapper.toPaymentResponse(mapped)).thenReturn(new PaymentResponse(null, "src", "dst", new BigDecimal("10.00"), type, "USD"));
            when(paymentMapper.toTransactionMessage(any(), any())).thenReturn(transactionMessage);

            PaymentResponse res = paymentService.create(userId, req);
            assertThat(res.type()).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("update: should convert data correctly")
    void shouldConvertDataCorrectly() {
        TransactionDto dto = new TransactionDto("id2", userId, payment.getId(), "src", "dst", new BigDecimal("50.00"), "EUR", TransactionType.DEBIT, TransactionStatus.PENDING);
        TransactionMessage msg = TransactionMessage.builder().userId(userId).data(dto).build();
        when(objectMapper.convertValue(dto, TransactionDto.class)).thenReturn(dto);
        when(paymentRepository.findById(payment.getId())).thenReturn(Optional.of(payment));
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(paymentResponse);

        PaymentResponse response = paymentService.update(msg);
        assertThat(response).isNotNull();
        verify(objectMapper).convertValue(dto, TransactionDto.class);
    }
}

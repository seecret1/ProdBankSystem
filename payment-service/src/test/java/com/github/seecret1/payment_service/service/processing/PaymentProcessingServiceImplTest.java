package com.github.seecret1.payment_service.service.processing;

import com.github.seecret1.payment_service.dto.message.TransactionMessage;
import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import com.github.seecret1.payment_service.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentProcessingServiceImpl Unit Tests")
class PaymentProcessingServiceImplTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentProcessingServiceImpl processingService;

    @Test
    @DisplayName("processMessage: should not throw even when implementation is empty (currently no-op)")
    void shouldNotThrowOnProcessMessage() {
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

        assertThatCode(() -> processingService.processMessage(message))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("processMessage: should handle null message without NPE")
    void shouldHandleNullMessage() {
        // Currently implementation is empty, so null is tolerated; documents current behavior
        assertThatCode(() -> processingService.processMessage(null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("processMessage: should handle message with null fields")
    void shouldHandleMessageWithNullFields() {
        var message = new TransactionMessage();
        assertThatCode(() -> processingService.processMessage(message))
                .doesNotThrowAnyException();
    }
}

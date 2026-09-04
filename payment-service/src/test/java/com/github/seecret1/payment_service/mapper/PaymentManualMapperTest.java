package com.github.seecret1.payment_service.mapper;

import com.github.seecret1.payment_service.dto.message.TransactionMessage;
import com.github.seecret1.payment_service.dto.payment.PaymentRequest;
import com.github.seecret1.payment_service.dto.payment.PaymentResponse;
import com.github.seecret1.payment_service.entity.Payment;
import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentManualMapper Unit Tests")
class PaymentManualMapperTest {

    private final PaymentManualMapper mapper = new PaymentManualMapper();

    @Test
    @DisplayName("toPayment: should map fields correctly")
    void shouldMapToPayment() {
        var request = new PaymentRequest("src-1", "dst-1", new BigDecimal("123.45"), PaymentType.CARD_PAYMENT, "RUB");
        String userId = "user-1";

        Payment result = mapper.toPayment(userId, request, PaymentStatus.CREATED);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getSourceInvoiceId()).isEqualTo("src-1");
        assertThat(result.getDestinationInvoiceId()).isEqualTo("dst-1");
        assertThat(result.getAmount()).isEqualByComparingTo("123.45");
        assertThat(result.getCurrency()).isEqualTo("RUB");
        assertThat(result.getPaymentType()).isEqualTo(PaymentType.CARD_PAYMENT);
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(result.getId()).isNull();
    }

    @Test
    @DisplayName("toTransactionMessage: should build message with traceId and data")
    void shouldBuildTransactionMessage() {
        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .userId("user-1")
                .sourceInvoiceId("src-1")
                .destinationInvoiceId("dst-1")
                .amount(new BigDecimal("100.00"))
                .currency("RUB")
                .paymentType(PaymentType.TRANSFER)
                .status(PaymentStatus.CREATED)
                .build();
        PaymentResponse response = new PaymentResponse(payment.getId(), "src-1", "dst-1", new BigDecimal("100.00"), PaymentType.TRANSFER, "RUB");

        TransactionMessage msg = mapper.toTransactionMessage(payment, response);

        assertThat(msg.getTraceId()).isNotBlank();
        assertThat(msg.getUserId()).isEqualTo("user-1");
        assertThat(msg.getSourceInvoiceId()).isEqualTo("src-1");
        assertThat(msg.getAmount()).isEqualByComparingTo("100.00");
        assertThat(msg.getPaymentType()).isEqualTo(PaymentType.TRANSFER);
        assertThat(msg.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(msg.getData()).isEqualTo(response);
        assertThat(msg.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("toPaymentResponse: should map entity to response")
    void shouldMapToResponse() {
        Payment payment = Payment.builder()
                .id("pid-123")
                .sourceInvoiceId("src")
                .destinationInvoiceId("dst")
                .amount(new BigDecimal("999.99"))
                .paymentType(PaymentType.DEPOSIT)
                .currency("USD")
                .build();

        PaymentResponse response = mapper.toPaymentResponse(payment);

        assertThat(response.id()).isEqualTo("pid-123");
        assertThat(response.sourceInvoiceId()).isEqualTo("src");
        assertThat(response.destinationInvoiceId()).isEqualTo("dst");
        assertThat(response.amount()).isEqualByComparingTo("999.99");
        assertThat(response.type()).isEqualTo(PaymentType.DEPOSIT);
        assertThat(response.currency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("toPayment: should handle null destinationInvoiceId")
    void shouldHandleNullDestination() {
        var request = new PaymentRequest("src", null, new BigDecimal("10.00"), PaymentType.WITHDRAWAL, "EUR");
        Payment result = mapper.toPayment("u1", request, PaymentStatus.CREATED);
        assertThat(result.getDestinationInvoiceId()).isNull();
    }

    @Test
    @DisplayName("toTransactionMessage: traceId should be unique per call")
    void shouldGenerateUniqueTraceId() {
        Payment payment = Payment.builder().userId("u").sourceInvoiceId("s").destinationInvoiceId("d").amount(BigDecimal.ONE).currency("RUB").paymentType(PaymentType.REFUND).status(PaymentStatus.CREATED).build();
        PaymentResponse r = new PaymentResponse("id", "s", "d", BigDecimal.ONE, PaymentType.REFUND, "RUB");
        var m1 = mapper.toTransactionMessage(payment, r);
        var m2 = mapper.toTransactionMessage(payment, r);
        assertThat(m1.getTraceId()).isNotEqualTo(m2.getTraceId());
    }

    @Test
    @DisplayName("toPayment: should preserve BigDecimal precision")
    void shouldPreservePrecision() {
        var request = new PaymentRequest("src", "dst", new BigDecimal("1234567890.12"), PaymentType.COMMISSION, "RUB");
        Payment p = mapper.toPayment("u", request, PaymentStatus.PROCESSING);
        assertThat(p.getAmount()).isEqualByComparingTo("1234567890.12");
    }
}

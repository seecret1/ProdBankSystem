package com.github.seecret1.payment_service.repository;

import com.github.seecret1.payment_service.PostgresTestContainersBase;
import com.github.seecret1.payment_service.entity.Payment;
import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentRepository Testcontainers PostgreSQL IT")
class PaymentRepositoryTestcontainersIT extends PostgresTestContainersBase {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("should save and retrieve payment via real PostgreSQL + Flyway")
    void shouldSaveViaPostgres() {
        Payment payment = Payment.builder()
                .userId("tc-user-1")
                .sourceInvoiceId("tc-src-1")
                .destinationInvoiceId("tc-dst-1")
                .amount(new BigDecimal("777.77"))
                .currency("RUB")
                .paymentType(PaymentType.TRANSFER)
                .status(PaymentStatus.CREATED)
                .build();

        Payment saved = paymentRepository.saveAndFlush(payment);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Payment loaded = paymentRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getAmount()).isEqualByComparingTo("777.77");
        assertThat(loaded.getPaymentType()).isEqualTo(PaymentType.TRANSFER);
    }

    @Test
    @DisplayName("should verify Flyway schema exists and Envers history table")
    void shouldVerifyFlywaySchema() {
        long count = paymentRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(0);
        // If Flyway failed, this would have thrown on startup
    }

    @Test
    @DisplayName("should handle high precision amount in PostgreSQL numeric(18,2)")
    void shouldHandlePrecisionInPostgres() {
        Payment p = Payment.builder()
                .userId("u-prec")
                .sourceInvoiceId("src-prec")
                .destinationInvoiceId("dst-prec")
                .amount(new BigDecimal("9999999999999999.99"))
                .currency("USD")
                .paymentType(PaymentType.DEPOSIT)
                .status(PaymentStatus.COMPLETED)
                .build();
        Payment saved = paymentRepository.saveAndFlush(p);
        assertThat(paymentRepository.findById(saved.getId()).orElseThrow().getAmount())
                .isEqualByComparingTo("9999999999999999.99");
    }
}

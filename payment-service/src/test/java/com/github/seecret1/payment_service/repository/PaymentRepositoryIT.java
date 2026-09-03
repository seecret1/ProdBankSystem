package com.github.seecret1.payment_service.repository;

import com.github.seecret1.payment_service.AbstractIntegrationTest;
import com.github.seecret1.payment_service.entity.Payment;
import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaymentRepository Integration Tests (H2)")
class PaymentRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("should save and find payment by id")
    void shouldSaveAndFindPayment() {
        Payment payment = Payment.builder()
                .userId("user-1")
                .sourceInvoiceId("src-1")
                .destinationInvoiceId("dst-1")
                .amount(new BigDecimal("500.00"))
                .currency("RUB")
                .paymentType(PaymentType.CARD_PAYMENT)
                .status(PaymentStatus.CREATED)
                .build();

        Payment saved = paymentRepository.save(payment);
        assertThat(saved.getId()).isNotNull();

        Optional<Payment> found = paymentRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualByComparingTo("500.00");
        assertThat(found.get().getStatus()).isEqualTo(PaymentStatus.CREATED);
    }

    @Test
    @DisplayName("should persist all PaymentStatus values")
    void shouldPersistAllStatuses() {
        for (PaymentStatus status : PaymentStatus.values()) {
            Payment p = Payment.builder()
                    .userId("u-" + status.name())
                    .sourceInvoiceId("src-" + status.name())
                    .destinationInvoiceId("dst")
                    .amount(new BigDecimal("10.00"))
                    .currency("EUR")
                    .paymentType(PaymentType.TRANSFER)
                    .status(status)
                    .build();
            Payment saved = paymentRepository.save(p);
            assertThat(saved.getId()).isNotNull();
            assertThat(paymentRepository.findById(saved.getId()).get().getStatus()).isEqualTo(status);
        }
    }

    @Test
    @DisplayName("should handle BigDecimal precision")
    void shouldHandleBigDecimalPrecision() {
        Payment payment = Payment.builder()
                .userId("user-precision")
                .sourceInvoiceId("src-p")
                .destinationInvoiceId("dst-p")
                .amount(new BigDecimal("1234567890.12"))
                .currency("USD")
                .paymentType(PaymentType.DEPOSIT)
                .status(PaymentStatus.COMPLETED)
                .build();
        Payment saved = paymentRepository.save(payment);
        Payment loaded = paymentRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getAmount()).isEqualByComparingTo("1234567890.12");
    }

    @Test
    @DisplayName("should count payments")
    void shouldCountPayments() {
        long before = paymentRepository.count();
        paymentRepository.save(Payment.builder().userId("u1").sourceInvoiceId("s1").destinationInvoiceId("d1").amount(BigDecimal.TEN).currency("RUB").paymentType(PaymentType.REFUND).status(PaymentStatus.CREATED).build());
        paymentRepository.save(Payment.builder().userId("u2").sourceInvoiceId("s2").destinationInvoiceId("d2").amount(BigDecimal.ONE).currency("RUB").paymentType(PaymentType.WITHDRAWAL).status(PaymentStatus.CREATED).build());
        assertThat(paymentRepository.count()).isEqualTo(before + 2);
    }

    @Test
    @DisplayName("should delete payment")
    void shouldDeletePayment() {
        Payment p = paymentRepository.save(Payment.builder().userId("u-del").sourceInvoiceId("s-del").destinationInvoiceId("d-del").amount(BigDecimal.TEN).currency("RUB").paymentType(PaymentType.COMMISSION).status(PaymentStatus.CREATED).build());
        String id = p.getId();
        paymentRepository.deleteById(id);
        assertThat(paymentRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("should handle null destinationInvoiceId")
    void shouldHandleNullDestination() {
        Payment p = Payment.builder().userId("u-null").sourceInvoiceId("s-null").destinationInvoiceId(null).amount(BigDecimal.TEN).currency("RUB").paymentType(PaymentType.WITHDRAWAL).status(PaymentStatus.CREATED).build();
        Payment saved = paymentRepository.save(p);
        assertThat(paymentRepository.findById(saved.getId()).get().getDestinationInvoiceId()).isNull();
    }
}

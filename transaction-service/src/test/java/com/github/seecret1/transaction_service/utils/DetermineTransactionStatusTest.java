package com.github.seecret1.transaction_service.utils;

import com.github.seecret1.transaction_service.entity.enums.PaymentStatus;
import com.github.seecret1.transaction_service.entity.enums.TransactionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DetermineTransactionStatus Unit Tests")
class DetermineTransactionStatusTest {

    @Test
    @DisplayName("should return PENDING when paymentStatus is null")
    void shouldReturnPendingForNull() {
        assertThat(DetermineTransactionStatus.getTransactionStatus(null))
                .isEqualTo(TransactionStatus.PENDING);
    }

    @ParameterizedTest(name = "{0} -> PENDING")
    @CsvSource({"CREATED", "PROCESSING"})
    void shouldMapToPending(String status) {
        assertThat(DetermineTransactionStatus.getTransactionStatus(PaymentStatus.valueOf(status)))
                .isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    @DisplayName("COMPLETED -> COMPLETED")
    void shouldMapToCompleted() {
        assertThat(DetermineTransactionStatus.getTransactionStatus(PaymentStatus.COMPLETED))
                .isEqualTo(TransactionStatus.COMPLETED);
    }

    @ParameterizedTest(name = "{0} -> FAILED")
    @CsvSource({"REJECTED", "FAILED", "CANCELLED"})
    void shouldMapToFailed(String status) {
        assertThat(DetermineTransactionStatus.getTransactionStatus(PaymentStatus.valueOf(status)))
                .isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    @DisplayName("should cover all PaymentStatus values")
    void shouldCoverAll() {
        for (PaymentStatus ps : PaymentStatus.values()) {
            assertThat(DetermineTransactionStatus.getTransactionStatus(ps)).isNotNull();
        }
    }
}

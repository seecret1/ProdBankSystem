package com.github.seecret1.transaction_service.utils;

import com.github.seecret1.transaction_service.entity.enums.PaymentType;
import com.github.seecret1.transaction_service.entity.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DetermineTransactionType Unit Tests")
class DetermineTransactionTypeTest {

    @Test
    @DisplayName("should return null when paymentType is null")
    void shouldReturnNullForNull() {
        assertThat(DetermineTransactionType.getTransactionType(null)).isNull();
    }

    @ParameterizedTest(name = "{0} -> DEBIT")
    @CsvSource({"CARD_PAYMENT", "WITHDRAWAL", "COMMISSION", "INSURANCE"})
    void shouldMapToDebit(String paymentType) {
        assertThat(DetermineTransactionType.getTransactionType(PaymentType.valueOf(paymentType)))
                .isEqualTo(TransactionType.DEBIT);
    }

    @ParameterizedTest(name = "{0} -> CREDIT")
    @CsvSource({"TRANSFER", "CREDIT_PAYMENT", "DEPOSIT", "REFUND"})
    void shouldMapToCredit(String paymentType) {
        assertThat(DetermineTransactionType.getTransactionType(PaymentType.valueOf(paymentType)))
                .isEqualTo(TransactionType.CREDIT);
    }

    @ParameterizedTest(name = "{0} -> FEE")
    @CsvSource({"CREDIT_INTEREST", "DEPOSIT_INTEREST"})
    void shouldMapToFee(String paymentType) {
        assertThat(DetermineTransactionType.getTransactionType(PaymentType.valueOf(paymentType)))
                .isEqualTo(TransactionType.FEE);
    }

    @Test
    @DisplayName("should cover all PaymentType values without exception")
    void shouldCoverAllValues() {
        for (PaymentType pt : PaymentType.values()) {
            TransactionType tt = DetermineTransactionType.getTransactionType(pt);
            assertThat(tt).isNotNull();
        }
    }
}

package com.github.seecret1.invoice_service.utils;

import com.github.seecret1.invoice_service.entity.enums.OperationType;
import com.github.seecret1.invoice_service.entity.enums.PaymentType;
import com.github.seecret1.invoice_service.entity.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OperationTypeHelper Unit Tests")
class OperationTypeHelperTest {

    @Test @DisplayName("should throw when transactionType is null")
    void shouldThrowWhenTransactionNull() {
        assertThatThrownBy(() -> OperationTypeHelper.determineOperationType(null, PaymentType.TRANSFER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TransactionType cannot be null");
    }

    @Test @DisplayName("should throw when paymentType is null")
    void shouldThrowWhenPaymentNull() {
        assertThatThrownBy(() -> OperationTypeHelper.determineOperationType(TransactionType.DEBIT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PaymentType cannot be null");
    }

    @Test @DisplayName("REVERSAL -> REFUND")
    void shouldMapReversal() {
        assertThat(OperationTypeHelper.determineOperationType(TransactionType.REVERSAL, PaymentType.TRANSFER)).isEqualTo(OperationType.REFUND);
    }

    @Test @DisplayName("FEE -> COMMISSION")
    void shouldMapFee() {
        assertThat(OperationTypeHelper.determineOperationType(TransactionType.FEE, PaymentType.COMMISSION)).isEqualTo(OperationType.COMMISSION);
    }

    @Test @DisplayName("DEBIT CARD_PAYMENT -> PAYMENT")
    void shouldMapDebitCardPayment() {
        assertThat(OperationTypeHelper.determineOperationType(TransactionType.DEBIT, PaymentType.CARD_PAYMENT)).isEqualTo(OperationType.PAYMENT);
    }

    @Test @DisplayName("DEBIT WITHDRAWAL -> WITHDRAWAL")
    void shouldMapDebitWithdrawal() {
        assertThat(OperationTypeHelper.determineOperationType(TransactionType.DEBIT, PaymentType.WITHDRAWAL)).isEqualTo(OperationType.WITHDRAWAL);
    }

    @Test @DisplayName("DEBIT DEPOSIT -> DEPOSIT")
    void shouldMapDebitDeposit() {
        assertThat(OperationTypeHelper.determineOperationType(TransactionType.DEBIT, PaymentType.DEPOSIT)).isEqualTo(OperationType.DEPOSIT);
    }

    @Test @DisplayName("CREDIT TRANSFER -> PAYMENT")
    void shouldMapCreditTransfer() {
        assertThat(OperationTypeHelper.determineOperationType(TransactionType.CREDIT, PaymentType.TRANSFER)).isEqualTo(OperationType.PAYMENT);
    }

    @Test @DisplayName("should cover all PaymentType for DEBIT and CREDIT")
    void shouldCoverAll() {
        for (PaymentType pt : PaymentType.values()) {
            assertThat(OperationTypeHelper.determineOperationType(TransactionType.DEBIT, pt)).isNotNull();
            assertThat(OperationTypeHelper.determineOperationType(TransactionType.CREDIT, pt)).isNotNull();
        }
    }
}

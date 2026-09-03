package com.github.seecret1.transaction_service.mapper;

import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.dto.transaction.TransactionDto;
import com.github.seecret1.transaction_service.entity.Transaction;
import com.github.seecret1.transaction_service.entity.enums.PaymentType;
import com.github.seecret1.transaction_service.entity.enums.TransactionStatus;
import com.github.seecret1.transaction_service.entity.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionMapper Unit Tests")
class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapper();

    @Test
    @DisplayName("toEntity: should map message fields correctly")
    void shouldMapToEntity() {
        var message = TransactionMessage.builder()
                .traceId(UUID.randomUUID().toString())
                .userId("user-1")
                .sourceInvoiceId("src-1")
                .destinationInvoiceId("dst-1")
                .amount(new BigDecimal("123.45"))
                .currency("RUB")
                .paymentType(PaymentType.TRANSFER)
                .build();

        Transaction entity = mapper.toEntity(message, "payment-123");

        assertThat(entity.getPaymentId()).isEqualTo("payment-123");
        assertThat(entity.getUserId()).isEqualTo("user-1");
        assertThat(entity.getSourceInvoiceId()).isEqualTo("src-1");
        assertThat(entity.getAmount()).isEqualByComparingTo("123.45");
        assertThat(entity.getCurrency()).isEqualTo("RUB");
        assertThat(entity.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(entity.getTransactionType()).isEqualTo(TransactionType.CREDIT); // TRANSFER -> CREDIT
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("toEntity: should map CARD_PAYMENT to DEBIT")
    void shouldMapCardPaymentToDebit() {
        var msg = TransactionMessage.builder().userId("u").sourceInvoiceId("s").destinationInvoiceId("d").amount(BigDecimal.TEN).currency("RUB").paymentType(PaymentType.CARD_PAYMENT).build();
        Transaction t = mapper.toEntity(msg, "pid");
        assertThat(t.getTransactionType()).isEqualTo(TransactionType.DEBIT);
    }

    @Test
    @DisplayName("toDto: should map entity to dto")
    void shouldMapToDto() {
        Transaction entity = Transaction.builder()
                .id("txn-1")
                .userId("user-1")
                .paymentId("pay-1")
                .sourceInvoiceId("src-1")
                .destinationInvoiceId("dst-1")
                .amount(new BigDecimal("99.99"))
                .currency("USD")
                .transactionType(TransactionType.FEE)
                .status(TransactionStatus.PROCESSING)
                .build();
        TransactionDto dto = mapper.toDto(entity);
        assertThat(dto.id()).isEqualTo("txn-1");
        assertThat(dto.paymentId()).isEqualTo("pay-1");
        assertThat(dto.transactionType()).isEqualTo(TransactionType.FEE);
        assertThat(dto.status()).isEqualTo(TransactionStatus.PROCESSING);
        assertThat(dto.amount()).isEqualByComparingTo("99.99");
    }

    @Test
    @DisplayName("toEntity: should handle null paymentType -> null transactionType")
    void shouldHandleNullPaymentType() {
        var msg = TransactionMessage.builder().userId("u").sourceInvoiceId("s").destinationInvoiceId("d").amount(BigDecimal.ONE).currency("RUB").paymentType(null).build();
        Transaction t = mapper.toEntity(msg, "pid");
        assertThat(t.getTransactionType()).isNull();
    }

    @Test
    @DisplayName("toEntity: should preserve BigDecimal precision")
    void shouldPreservePrecision() {
        var msg = TransactionMessage.builder().userId("u").sourceInvoiceId("s").destinationInvoiceId("d").amount(new BigDecimal("1234567890.12")).currency("RUB").paymentType(PaymentType.DEPOSIT).build();
        Transaction t = mapper.toEntity(msg, "pid");
        assertThat(t.getAmount()).isEqualByComparingTo("1234567890.12");
    }
}

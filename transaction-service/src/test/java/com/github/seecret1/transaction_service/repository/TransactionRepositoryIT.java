package com.github.seecret1.transaction_service.repository;

import com.github.seecret1.transaction_service.AbstractIntegrationTest;
import com.github.seecret1.transaction_service.entity.Transaction;
import com.github.seecret1.transaction_service.entity.enums.TransactionStatus;
import com.github.seecret1.transaction_service.entity.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionRepository Integration Tests (H2)")
class TransactionRepositoryIT extends AbstractIntegrationTest {

    @Autowired private TransactionRepository repository;

    @Test @DisplayName("should save and find by id")
    void shouldSaveAndFind() {
        Transaction t = Transaction.builder().paymentId("pay-1").userId("u1").sourceInvoiceId("src1").destinationInvoiceId("dst1").amount(new BigDecimal("100.00")).currency("RUB").transactionType(TransactionType.CREDIT).status(TransactionStatus.PENDING).build();
        Transaction saved = repository.save(t);
        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
    }

    @Test @DisplayName("should persist all TransactionType values")
    void shouldPersistAllTypes() {
        for (TransactionType type : TransactionType.values()) {
            Transaction t = Transaction.builder().paymentId("pay-" + type).userId("u-" + type).sourceInvoiceId("src-" + type).destinationInvoiceId("dst-" + type).amount(BigDecimal.TEN).currency("RUB").transactionType(type).status(TransactionStatus.PENDING).build();
            Transaction saved = repository.save(t);
            assertThat(repository.findById(saved.getId()).orElseThrow().getTransactionType()).isEqualTo(type);
        }
    }

    @Test @DisplayName("should persist all TransactionStatus values")
    void shouldPersistAllStatuses() {
        for (TransactionStatus st : TransactionStatus.values()) {
            Transaction t = Transaction.builder().paymentId("pay-" + st).userId("u-" + st).sourceInvoiceId("src-" + st).destinationInvoiceId("dst-" + st).amount(BigDecimal.TEN).currency("USD").transactionType(TransactionType.DEBIT).status(st).build();
            Transaction saved = repository.save(t);
            assertThat(repository.findById(saved.getId()).orElseThrow().getStatus()).isEqualTo(st);
        }
    }

    @Test @DisplayName("should count and delete")
    void shouldCountAndDelete() {
        long before = repository.count();
        Transaction t = repository.save(Transaction.builder().paymentId("pay-del").userId("u-del").sourceInvoiceId("s-del").destinationInvoiceId("d-del").amount(BigDecimal.ONE).currency("RUB").transactionType(TransactionType.FEE).status(TransactionStatus.PENDING).build());
        assertThat(repository.count()).isEqualTo(before + 1);
        repository.deleteById(t.getId());
        assertThat(repository.findById(t.getId())).isEmpty();
    }

    @Test @DisplayName("should handle high precision amount")
    void shouldHandlePrecision() {
        Transaction t = Transaction.builder().paymentId("pay-prec").userId("u-prec").sourceInvoiceId("src-prec").destinationInvoiceId("dst-prec").amount(new BigDecimal("9999999999999999.99")).currency("EUR").transactionType(TransactionType.CREDIT).status(TransactionStatus.COMPLETED).build();
        Transaction saved = repository.save(t);
        assertThat(repository.findById(saved.getId()).orElseThrow().getAmount()).isEqualByComparingTo("9999999999999999.99");
    }
}

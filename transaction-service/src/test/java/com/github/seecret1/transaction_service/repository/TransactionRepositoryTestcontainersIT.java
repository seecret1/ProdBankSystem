package com.github.seecret1.transaction_service.repository;

import com.github.seecret1.transaction_service.PostgresTestContainersBase;
import com.github.seecret1.transaction_service.entity.Transaction;
import com.github.seecret1.transaction_service.entity.enums.TransactionStatus;
import com.github.seecret1.transaction_service.entity.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionRepository Testcontainers PostgreSQL IT")
class TransactionRepositoryTestcontainersIT extends PostgresTestContainersBase {

    @Autowired private TransactionRepository repository;

    @Test @DisplayName("should save via real PostgreSQL + Flyway")
    void shouldSaveViaPostgres() {
        Transaction t = Transaction.builder().paymentId("tc-pay-1").userId("tc-u1").sourceInvoiceId("tc-src1").destinationInvoiceId("tc-dst1").amount(new BigDecimal("777.77")).currency("RUB").transactionType(TransactionType.CREDIT).status(TransactionStatus.PENDING).build();
        Transaction saved = repository.saveAndFlush(t);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        Transaction loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getAmount()).isEqualByComparingTo("777.77");
    }

    @Test @DisplayName("should verify Flyway schema works")
    void shouldVerifySchema() {
        assertThat(repository.count()).isGreaterThanOrEqualTo(0);
    }
}

package com.github.seecret1.invoice_service.repository;

import com.github.seecret1.invoice_service.PostgresTestContainersBase;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.enums.InvoiceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Invoice Repositories Testcontainers PostgreSQL IT")
class InvoiceTestcontainersIT extends PostgresTestContainersBase {

    @Autowired private CardInvoiceRepository cardInvoiceRepository;
    @Autowired private OperationRepository operationRepository;

    @Test @DisplayName("should save CardInvoice via real PostgreSQL + Flyway")
    void shouldSaveViaPostgres() {
        CardInvoice inv = CardInvoice.builder().cardId("tc-card-1").invoiceNumber("TC-INV-1").userId("tc-user-1").currency("RUB").status(InvoiceStatus.ACTIVE).balance(new BigDecimal("5000.00")).spendingLimit(new BigDecimal("200000")).freeLimit(new BigDecimal("100000")).deleted(false).build();
        CardInvoice saved = cardInvoiceRepository.saveAndFlush(inv);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(cardInvoiceRepository.findByCardId("tc-card-1")).isPresent();
    }

    @Test @DisplayName("should verify Flyway schema and count")
    void shouldVerifySchema() {
        assertThat(cardInvoiceRepository.count()).isGreaterThanOrEqualTo(0);
        assertThat(operationRepository.count()).isGreaterThanOrEqualTo(0);
    }
}

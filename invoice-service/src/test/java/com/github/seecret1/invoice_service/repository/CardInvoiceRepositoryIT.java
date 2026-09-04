package com.github.seecret1.invoice_service.repository;

import com.github.seecret1.invoice_service.AbstractIntegrationTest;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.enums.InvoiceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CardInvoiceRepository Integration Tests (H2)")
class CardInvoiceRepositoryIT extends AbstractIntegrationTest {

    @Autowired private CardInvoiceRepository repository;

    private CardInvoice newInvoice(String cardId, String invoiceNumber) {
        return CardInvoice.builder().cardId(cardId).invoiceNumber(invoiceNumber).userId("user-" + cardId).currency("RUB").status(InvoiceStatus.ACTIVE).balance(new BigDecimal("1000.00")).spendingLimit(new BigDecimal("200000")).freeLimit(new BigDecimal("100000")).deleted(false).build();
    }

    @Test @DisplayName("should save and find by id not deleted")
    void shouldSaveAndFindNotDeleted() {
        CardInvoice inv = repository.save(newInvoice("card-1", "INV-1"));
        assertThat(inv.getId()).isNotNull();
        assertThat(repository.findByIdNotDeleted(inv.getId())).isPresent();
    }

    @Test @DisplayName("should not find deleted invoice via findByIdNotDeleted")
    void shouldNotFindDeleted() {
        CardInvoice inv = repository.saveAndFlush(newInvoice("card-del", "INV-DEL"));
        CardInvoice persisted = repository.findById(inv.getId()).orElseThrow();
        persisted.softDelete("admin");
        repository.saveAndFlush(persisted);
        assertThat(repository.findByIdNotDeleted(persisted.getId())).isEmpty();
        assertThat(repository.findByIdIncludingDeleted(persisted.getId())).isPresent();
    }

    @Test @DisplayName("should find by cardId")
    void shouldFindByCardId() {
        CardInvoice inv = repository.save(newInvoice("card-find", "INV-FIND"));
        assertThat(repository.findByCardId("card-find")).isPresent();
        assertThat(repository.findByCardId("non-existent")).isEmpty();
    }

    @Test @DisplayName("should check exists by invoiceNumber and cardId")
    void shouldCheckExists() {
        repository.save(newInvoice("card-exists", "INV-EXISTS"));
        assertThat(repository.existsByInvoiceNumber("INV-EXISTS")).isTrue();
        assertThat(repository.existsByCardId("card-exists")).isTrue();
        assertThat(repository.existsByInvoiceNumber("NOT-EXISTS")).isFalse();
    }

    @Test @DisplayName("should find by userId")
    void shouldFindByUserId() {
        repository.saveAndFlush(newInvoice("card-u1-1", "INV-U1-1"));
        repository.saveAndFlush(newInvoice("card-u1-2", "INV-U1-2"));
        // newInvoice sets userId as "user-" + cardId, so check user-card-u1-1
        assertThat(repository.findByUserId("user-card-u1-1")).hasSize(1);
    }

    @Test @DisplayName("should find with pessimistic lock")
    void shouldFindWithLock() {
        CardInvoice inv = repository.save(newInvoice("card-lock", "INV-LOCK"));
        assertThat(repository.findByIdWithLock(inv.getId())).isPresent();
        assertThat(repository.findByIdForUpdate(inv.getId())).isPresent();
    }

    @Test @DisplayName("should paginate not deleted")
    void shouldPaginate() {
        repository.save(newInvoice("card-page-1", "INV-PAGE-1"));
        repository.save(newInvoice("card-page-2", "INV-PAGE-2"));
        var page = repository.findAllNotDeleted(org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    }
}

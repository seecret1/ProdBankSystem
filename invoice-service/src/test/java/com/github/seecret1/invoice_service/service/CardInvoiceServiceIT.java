package com.github.seecret1.invoice_service.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.invoice_service.AbstractIntegrationTest;
import com.github.seecret1.invoice_service.dto.request.CardInvoiceCreateRequest;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.entity.enums.InvoiceStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CardInvoiceService Integration Tests (H2)")
class CardInvoiceServiceIT extends AbstractIntegrationTest {

    @Autowired private CardInvoiceService cardInvoiceService;

    private CardInvoiceCreateRequest newRequest(String cardId, String invoiceNumber) {
        return new CardInvoiceCreateRequest(cardId, "user-" + UUID.randomUUID(), invoiceNumber, "RUB", new BigDecimal("1000.00"), new BigDecimal("200000"), new BigDecimal("100000"));
    }

    @Test @DisplayName("create and find by id")
    void shouldCreateAndFind() {
        String cardId = "card-it-" + UUID.randomUUID();
        String invNum = "INV-IT-" + UUID.randomUUID();
        CardInvoiceResponse created = cardInvoiceService.create(newRequest(cardId, invNum));
        assertThat(created.id()).isNotNull();
        assertThat(created.cardId()).isEqualTo(cardId);
        CardInvoiceResponse found = cardInvoiceService.findById(created.id());
        assertThat(found.invoiceNumber()).isEqualTo(invNum);
    }

    @Test @DisplayName("findByCardId: should return correct invoice")
    void shouldFindByCardId() {
        String cardId = "card-find-" + UUID.randomUUID();
        String invNum = "INV-FIND-" + UUID.randomUUID();
        CardInvoiceResponse created = cardInvoiceService.create(newRequest(cardId, invNum));
        CardInvoiceResponse found = cardInvoiceService.findByCardId(cardId);
        assertThat(found.id()).isEqualTo(created.id());
    }

    @Test @DisplayName("findAll: should paginate")
    void shouldPaginate() {
        cardInvoiceService.create(newRequest("card-page-" + UUID.randomUUID(), "INV-PAGE-" + UUID.randomUUID()));
        cardInvoiceService.create(newRequest("card-page2-" + UUID.randomUUID(), "INV-PAGE2-" + UUID.randomUUID()));
        PageResponse<CardInvoiceResponse> page = cardInvoiceService.findAll(new PageModel(0, 10));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getData()).isNotEmpty();
    }

    @Test @DisplayName("softDelete: should mark deleted and hide from findAll")
    void shouldSoftDelete() {
        String cardId = "card-del-" + UUID.randomUUID();
        String invNum = "INV-DEL-" + UUID.randomUUID();
        CardInvoiceResponse created = cardInvoiceService.create(newRequest(cardId, invNum));
        cardInvoiceService.softDelete(created.id(), "tester");
        assertThatThrownBy(() -> cardInvoiceService.findById(created.id()))
                .isInstanceOf(com.github.seecret1.invoice_service.exception.InvoiceNotFoundException.class);
        // hard delete cleans up
        cardInvoiceService.hardDelete(created.id());
        assertThatThrownBy(() -> cardInvoiceService.findById(created.id()))
                .isInstanceOf(com.github.seecret1.invoice_service.exception.InvoiceNotFoundException.class);
    }

    @Test @DisplayName("create: should fail on duplicate invoiceNumber")
    void shouldFailDuplicate() {
        String cardId = "card-dup-" + UUID.randomUUID();
        String invNum = "INV-DUP-" + UUID.randomUUID();
        cardInvoiceService.create(newRequest(cardId, invNum));
        String cardId2 = "card-dup2-" + UUID.randomUUID();
        assertThatThrownBy(() -> cardInvoiceService.create(newRequest(cardId2, invNum)))
                .isInstanceOf(com.github.seecret1.invoice_service.exception.InvoiceAlreadyExistsException.class);
    }
}

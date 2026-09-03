package com.github.seecret1.invoice_service.service;

import com.github.seecret1.invoice_service.AbstractIntegrationTest;
import com.github.seecret1.invoice_service.dto.message.TransactionMessage;
import com.github.seecret1.invoice_service.dto.transaction.TransactionDto;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.enums.*;
import com.github.seecret1.invoice_service.repository.CardInvoiceRepository;
import com.github.seecret1.invoice_service.repository.OperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionService Integration Tests (H2)")
class TransactionServiceIT extends AbstractIntegrationTest {

    @Autowired private TransactionService transactionService;
    @Autowired private CardInvoiceRepository invoiceRepository;
    @Autowired private OperationRepository operationRepository;

    private CardInvoice source;
    private CardInvoice dest;

    @BeforeEach
    void setUp() {
        source = invoiceRepository.save(CardInvoice.builder().cardId("src-card-" + UUID.randomUUID()).invoiceNumber("INV-SRC-" + UUID.randomUUID()).userId("user-src").currency("RUB").status(InvoiceStatus.ACTIVE).balance(new BigDecimal("10000.00")).spendingLimit(new BigDecimal("5000.00")).freeLimit(new BigDecimal("1000.00")).deleted(false).build());
        dest = invoiceRepository.save(CardInvoice.builder().cardId("dst-card-" + UUID.randomUUID()).invoiceNumber("INV-DST-" + UUID.randomUUID()).userId("user-dst").currency("RUB").status(InvoiceStatus.ACTIVE).balance(new BigDecimal("5000.00")).spendingLimit(new BigDecimal("5000.00")).freeLimit(new BigDecimal("1000.00")).deleted(false).build());
    }

    @Test @DisplayName("transactionProcessing: should complete successfully and update balances")
    void shouldCompleteSuccessfully() {
        TransactionDto dto = new TransactionDto("user-src", "pay-1", source.getId(), dest.getId(), new BigDecimal("500.00"), "RUB", TransactionType.DEBIT, TransactionStatus.PENDING);
        TransactionMessage msg = TransactionMessage.builder().traceId(UUID.randomUUID().toString()).userId("user-src").sourceInvoiceId(source.getId()).destinationInvoiceId(dest.getId()).amount(new BigDecimal("500.00")).currency("RUB").paymentType(PaymentType.CARD_PAYMENT).status(PaymentStatus.PROCESSING).data(dto).build();

        TransactionMessage result = transactionService.transactionProcessing(msg);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        CardInvoice updatedSource = invoiceRepository.findByIdWithLock(source.getId()).orElseThrow();
        CardInvoice updatedDest = invoiceRepository.findByIdWithLock(dest.getId()).orElseThrow();
        assertThat(updatedSource.getBalance()).isEqualByComparingTo("9500.00");
        assertThat(updatedDest.getBalance()).isEqualByComparingTo("5500.00");
        assertThat(operationRepository.count()).isGreaterThanOrEqualTo(2);
    }

    @Test @DisplayName("transactionProcessing: should fail on insufficient balance")
    void shouldFailInsufficientBalance() {
        source.setBalance(new BigDecimal("10.00"));
        invoiceRepository.save(source);
        TransactionDto dto = new TransactionDto("user-src", "pay-2", source.getId(), dest.getId(), new BigDecimal("500.00"), "RUB", TransactionType.DEBIT, TransactionStatus.PENDING);
        TransactionMessage msg = TransactionMessage.builder().traceId(UUID.randomUUID().toString()).userId("user-src").sourceInvoiceId(source.getId()).destinationInvoiceId(dest.getId()).amount(new BigDecimal("500.00")).currency("RUB").paymentType(PaymentType.CARD_PAYMENT).data(dto).build();

        TransactionMessage result = transactionService.transactionProcessing(msg);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        // balances unchanged
        assertThat(invoiceRepository.findById(source.getId()).orElseThrow().getBalance()).isEqualByComparingTo("10.00");
    }

    @Test @DisplayName("transactionProcessing: should fail on currency mismatch")
    void shouldFailCurrencyMismatch() {
        dest.setCurrency("USD");
        invoiceRepository.save(dest);
        TransactionDto dto = new TransactionDto("user-src", "pay-3", source.getId(), dest.getId(), new BigDecimal("100.00"), "RUB", TransactionType.CREDIT, TransactionStatus.PENDING);
        TransactionMessage msg = TransactionMessage.builder().traceId(UUID.randomUUID().toString()).userId("user-src").sourceInvoiceId(source.getId()).destinationInvoiceId(dest.getId()).amount(new BigDecimal("100.00")).currency("RUB").paymentType(PaymentType.TRANSFER).data(dto).build();

        TransactionMessage result = transactionService.transactionProcessing(msg);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test @DisplayName("transactionProcessing: should handle blocked invoice")
    void shouldFailBlocked() {
        source.setStatus(InvoiceStatus.BLOCKED);
        invoiceRepository.save(source);
        TransactionDto dto = new TransactionDto("user-src", "pay-4", source.getId(), dest.getId(), new BigDecimal("100.00"), "RUB", TransactionType.DEBIT, TransactionStatus.PENDING);
        TransactionMessage msg = TransactionMessage.builder().traceId(UUID.randomUUID().toString()).userId("user-src").sourceInvoiceId(source.getId()).destinationInvoiceId(dest.getId()).amount(new BigDecimal("100.00")).currency("RUB").paymentType(PaymentType.CARD_PAYMENT).data(dto).build();

        TransactionMessage result = transactionService.transactionProcessing(msg);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}

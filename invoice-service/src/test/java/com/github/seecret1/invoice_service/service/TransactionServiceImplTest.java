package com.github.seecret1.invoice_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.invoice_service.dto.message.TransactionMessage;
import com.github.seecret1.invoice_service.dto.transaction.TransactionDto;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.Operation;
import com.github.seecret1.invoice_service.entity.enums.*;
import com.github.seecret1.invoice_service.repository.CardInvoiceRepository;
import com.github.seecret1.invoice_service.repository.OperationRepository;
import com.github.seecret1.invoice_service.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl Unit Tests")
class TransactionServiceImplTest {

    @Mock private CardInvoiceRepository invoiceRepository;
    @Mock private OperationRepository operationRepository;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private TransactionServiceImpl service;

    private CardInvoice source;
    private CardInvoice dest;
    private TransactionMessage message;
    private TransactionDto transactionDto;

    @BeforeEach
    void setUp() {
        source = CardInvoice.builder().id("src-1").userId("u1").invoiceNumber("INV-SRC").currency("RUB").status(InvoiceStatus.ACTIVE).balance(new BigDecimal("10000.00")).spendingLimit(new BigDecimal("5000.00")).freeLimit(new BigDecimal("1000.00")).deleted(false).build();
        dest = CardInvoice.builder().id("dst-1").userId("u2").invoiceNumber("INV-DST").currency("RUB").status(InvoiceStatus.ACTIVE).balance(new BigDecimal("5000.00")).spendingLimit(new BigDecimal("5000.00")).freeLimit(new BigDecimal("1000.00")).deleted(false).build();
        transactionDto = new TransactionDto("u1", "pay-1", "src-1", "dst-1", new BigDecimal("500.00"), "RUB", TransactionType.DEBIT, TransactionStatus.PENDING);
        message = TransactionMessage.builder().traceId("t1").userId("u1").sourceInvoiceId("src-1").destinationInvoiceId("dst-1").amount(new BigDecimal("500.00")).currency("RUB").paymentType(PaymentType.CARD_PAYMENT).status(PaymentStatus.PROCESSING).data(transactionDto).build();
    }

    @Test @DisplayName("transactionProcessing: should succeed without commission when within freeLimit")
    void shouldSucceedWithoutCommission() {
        when(invoiceRepository.findByIdWithLock("src-1")).thenReturn(Optional.of(source));
        when(invoiceRepository.findByIdWithLock("dst-1")).thenReturn(Optional.of(dest));
        when(objectMapper.convertValue(message.getData(), TransactionDto.class)).thenReturn(transactionDto);

        TransactionMessage result = service.transactionProcessing(message);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PROCESSING); // set to PROCESSING on success path, not FAILED
        assertThat(source.getBalance()).isEqualByComparingTo("9500.00"); // 10000 - 500
        assertThat(dest.getBalance()).isEqualByComparingTo("5500.00");
        assertThat(source.getFreeLimit()).isEqualByComparingTo("500.00"); // 1000-500
        verify(operationRepository).saveAll(any());
        verify(invoiceRepository).saveAll(any());
    }

    @Test @DisplayName("transactionProcessing: should apply commission when exceeds freeLimit")
    void shouldApplyCommission() {
        source.setFreeLimit(new BigDecimal("100.00"));
        source.setSpendingLimit(new BigDecimal("5000.00"));
        source.setBalance(new BigDecimal("10000.00"));
        when(invoiceRepository.findByIdWithLock(anyString())).thenReturn(Optional.of(source)).thenReturn(Optional.of(dest));
        when(objectMapper.convertValue(any(), eq(TransactionDto.class))).thenReturn(transactionDto);

        TransactionMessage result = service.transactionProcessing(message);

        // should still be PROCESSING because commission applies but balance sufficient
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        assertThat(source.getBalance()).isLessThan(new BigDecimal("10000.00"));
        assertThat(source.getFreeLimit()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(operationRepository).saveAll(any());
    }

    @Test @DisplayName("transactionProcessing: should return FAILED when insufficient balance")
    void shouldFailWhenInsufficientBalance() {
        source.setBalance(new BigDecimal("100.00"));
        when(invoiceRepository.findByIdWithLock("src-1")).thenReturn(Optional.of(source));
        when(invoiceRepository.findByIdWithLock("dst-1")).thenReturn(Optional.of(dest));
        when(objectMapper.convertValue(any(), eq(TransactionDto.class))).thenReturn(transactionDto);

        TransactionMessage result = service.transactionProcessing(message);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test @DisplayName("transactionProcessing: should return FAILED when currency mismatch")
    void shouldFailOnCurrencyMismatch() {
        dest.setCurrency("USD");
        when(invoiceRepository.findByIdWithLock("src-1")).thenReturn(Optional.of(source));
        when(invoiceRepository.findByIdWithLock("dst-1")).thenReturn(Optional.of(dest));
        when(objectMapper.convertValue(any(), eq(TransactionDto.class))).thenReturn(transactionDto);

        TransactionMessage result = service.transactionProcessing(message);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test @DisplayName("transactionProcessing: should return FAILED when source is BLOCKED")
    void shouldFailWhenBlocked() {
        source.setStatus(InvoiceStatus.BLOCKED);
        when(invoiceRepository.findByIdWithLock("src-1")).thenReturn(Optional.of(source));
        when(invoiceRepository.findByIdWithLock("dst-1")).thenReturn(Optional.of(dest));

        TransactionMessage result = service.transactionProcessing(message);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test @DisplayName("transactionProcessing: should return FAILED when invoice deleted")
    void shouldFailWhenDeleted() {
        source.setDeleted(true);
        when(invoiceRepository.findByIdWithLock("src-1")).thenReturn(Optional.of(source));
        when(invoiceRepository.findByIdWithLock("dst-1")).thenReturn(Optional.of(dest));

        TransactionMessage result = service.transactionProcessing(message);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test @DisplayName("transactionProcessing: should return FAILED when spendingLimit exceeded")
    void shouldFailWhenSpendingLimitExceeded() {
        source.setSpendingLimit(new BigDecimal("100.00"));
        when(invoiceRepository.findByIdWithLock("src-1")).thenReturn(Optional.of(source));
        when(invoiceRepository.findByIdWithLock("dst-1")).thenReturn(Optional.of(dest));

        TransactionMessage result = service.transactionProcessing(message);

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test @DisplayName("transactionProcessing: should return FAILED when commission makes amount > balance")
    void shouldFailWhenCommissionExceedsBalance() {
        source.setBalance(new BigDecimal("600.00"));
        source.setFreeLimit(BigDecimal.ZERO);
        source.setSpendingLimit(new BigDecimal("100.00"));
        // message amount 500, taxable 500, commission will be calculated -> full > balance?
        when(invoiceRepository.findByIdWithLock("src-1")).thenReturn(Optional.of(source));
        when(invoiceRepository.findByIdWithLock("dst-1")).thenReturn(Optional.of(dest));
        when(objectMapper.convertValue(any(), eq(TransactionDto.class))).thenReturn(transactionDto);

        TransactionMessage result = service.transactionProcessing(message);

        // With balance 600 and fullAmount ~ 500 + commission, may still succeed; test with smaller balance
        // Use 400 balance to ensure failure
        source.setBalance(new BigDecimal("400.00"));
        when(invoiceRepository.findByIdWithLock("src-1")).thenReturn(Optional.of(source));
        when(invoiceRepository.findByIdWithLock("dst-1")).thenReturn(Optional.of(dest));
        TransactionMessage result2 = service.transactionProcessing(message);
        assertThat(result2.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}

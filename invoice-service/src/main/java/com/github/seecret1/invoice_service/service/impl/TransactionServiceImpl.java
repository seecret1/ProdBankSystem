package com.github.seecret1.invoice_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.invoice_service.dto.message.TransactionMessage;
import com.github.seecret1.invoice_service.dto.transaction.TransactionDto;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.Operation;
import com.github.seecret1.invoice_service.entity.enums.InvoiceStatus;
import com.github.seecret1.invoice_service.entity.enums.OperationType;
import com.github.seecret1.invoice_service.entity.enums.PaymentStatus;
import com.github.seecret1.invoice_service.repository.CardInvoiceRepository;
import com.github.seecret1.invoice_service.repository.OperationRepository;
import com.github.seecret1.invoice_service.service.TransactionService;
import com.github.seecret1.invoice_service.utils.OperationTypeHelper;
import com.github.seecret1.invoice_service.utils.PercentUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final CardInvoiceRepository invoiceRepository;

    private final OperationRepository operationRepository;

    private final ObjectMapper objectMapper;

    //TODO: добавить метрики с длительностью транзакции
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionMessage transactionProcessing(TransactionMessage message) {
        try {
            log.info("Processing transaction: {}", message);
            message.setStatus(PaymentStatus.PROCESSING);

            var sourceInvoice = findById(message.getSourceInvoiceId());
            var destinationInvoice = findById(message.getDestinationInvoiceId());

            validation(sourceInvoice, destinationInvoice, message);

            BigDecimal sourceInvoiceBalance = sourceInvoice.getBalance();
            BigDecimal destinationInvoiceBalance = destinationInvoice.getBalance();

            TransactionDto transactionDto = objectMapper.convertValue(message.getData(), TransactionDto.class);

            var amount = message.getAmount();
            var fullSourceAmount = amount;
            var commissionAmount = BigDecimal.ZERO;
            var commissionPercent = BigDecimal.ZERO;
            var operationType = OperationTypeHelper.determineOperationType(transactionDto.transactionType(), message.getPaymentType());

            Operation sourceOperation = Operation.builder()
                    .amount(amount)
                    .operationType(operationType)
                    .build();
            var freeLimit = sourceInvoice.getFreeLimit();
            var spendingLimit = sourceInvoice.getSpendingLimit();

            if (freeLimit.compareTo(amount) >= 0) {
                sourceInvoice.setFreeLimit(freeLimit.subtract(amount));
                sourceInvoice.setSpendingLimit(spendingLimit.subtract(amount));
                sourceOperation.setCommissionPercent(commissionPercent);
                sourceOperation.setCommissionAmount(commissionAmount);
                sourceOperation.setFullAmount(fullSourceAmount);
                log.debug("There is no fee in the transaction");
            } else {
                var taxableAmount = amount.subtract(freeLimit);
                commissionAmount = PercentUtils.getAdditionalAmount(sourceInvoice, taxableAmount);
                commissionPercent = PercentUtils.getPercent(sourceInvoice, taxableAmount);
                fullSourceAmount = amount.add(commissionAmount);

                if (sourceInvoice.getBalance().compareTo(fullSourceAmount) < 0) {
                    message.setStatus(PaymentStatus.REJECTED);
                    throw new IllegalArgumentException("new amount > source invoice balance");
                }

                sourceOperation.setCommissionPercent(commissionPercent);
                sourceOperation.setCommissionAmount(commissionAmount);
                sourceInvoice.setFreeLimit(BigDecimal.ZERO);
                sourceInvoice.setSpendingLimit(spendingLimit.subtract(amount));
                sourceOperation.setFullAmount(fullSourceAmount);
                log.debug("The transaction includes fees");
            }

            Operation destinationOperation = createDestinationOperation(amount, operationType);
            log.debug("Create operation in destination invoice: {}", destinationOperation);

            operationRepository.saveAll(List.of(sourceOperation, destinationOperation));

            finishedTransaction(
                    sourceInvoice,
                    sourceInvoiceBalance,
                    fullSourceAmount,
                    destinationInvoice,
                    destinationInvoiceBalance,
                    amount,
                    sourceOperation,
                    destinationOperation
            );
            invoiceRepository.saveAll(List.of(sourceInvoice, destinationInvoice));

            log.info("Transaction successfully processing");
            return message;

        } catch (Exception ex) {
            log.error("Failed to processing transaction");
            message.setStatus(PaymentStatus.FAILED);
            return message;
        }
    }

    private void finishedTransaction(
            CardInvoice sourceInvoice,
            BigDecimal sourceInvoiceBalance,
            BigDecimal fullSourceAmount,
            CardInvoice destinationInvoice,
            BigDecimal destinationInvoiceBalance,
            BigDecimal amount,
            Operation sourceOperation,
            Operation destinationOperation
    ) {
        sourceInvoice.setBalance(sourceInvoiceBalance.subtract(fullSourceAmount));
        destinationInvoice.setBalance(destinationInvoiceBalance.add(amount));
        sourceInvoice.setOperation(sourceOperation);
        destinationInvoice.setOperation(destinationOperation);
    }

    private void validation(
            CardInvoice sourceInvoice,
            CardInvoice destinationInvoice,
            TransactionMessage message
    ) {
        if (Boolean.TRUE.equals(sourceInvoice.getDeleted()) ||
                Boolean.TRUE.equals(destinationInvoice.getDeleted())
        ) {
            message.setStatus(PaymentStatus.REJECTED);
            throw new IllegalArgumentException("One or both invoices are deleted");
        }
        if (sourceInvoice.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            message.setStatus(PaymentStatus.REJECTED);
            throw new IllegalArgumentException("Source invoice balance must be greater than zero");
        }
        if (sourceInvoice.getBalance().compareTo(message.getAmount()) < 0) {
            message.setStatus(PaymentStatus.REJECTED);
            throw new IllegalArgumentException("Insufficient balance on source invoice");
        }
        if (sourceInvoice.getSpendingLimit().compareTo(message.getAmount()) < 0) {
            message.setStatus(PaymentStatus.REJECTED);
            throw new IllegalArgumentException("Transaction amount exceeds free limit on source invoice");
        }
        if (!(sourceInvoice.getCurrency().equals(message.getCurrency()) &&
                destinationInvoice.getCurrency().equals(message.getCurrency()))) {
            message.setStatus(PaymentStatus.REJECTED);
            throw new IllegalArgumentException("Currency mismatch: invoice currencies do not match transaction currency");
        }
        if (sourceInvoice.getStatus() == InvoiceStatus.BLOCKED ||
                sourceInvoice.getStatus() == InvoiceStatus.FREEZE) {
            message.setStatus(PaymentStatus.REJECTED);
            throw new IllegalArgumentException("Source invoice is blocked or frozen");
        }
        if (destinationInvoice.getStatus() == InvoiceStatus.BLOCKED ||
                destinationInvoice.getStatus() == InvoiceStatus.FREEZE) {
            message.setStatus(PaymentStatus.REJECTED);
            throw new IllegalArgumentException("Destination invoice is blocked or frozen");
        }
    }

    private CardInvoice findById(String id) {
        return invoiceRepository.findByIdWithLock(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Invoice not found by ID: " + id
                ));
    }

    private Operation createDestinationOperation(
            BigDecimal amount,
            OperationType operationType
    ) {
        return Operation.builder()
                .amount(amount)
                .fullAmount(amount)
                .commissionAmount(BigDecimal.ZERO)
                .commissionPercent(BigDecimal.ZERO)
                .operationType(operationType)
                .build();
    }
}

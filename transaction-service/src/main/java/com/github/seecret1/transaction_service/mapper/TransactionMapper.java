package com.github.seecret1.transaction_service.mapper;

import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.dto.transaction.TransactionDto;
import com.github.seecret1.transaction_service.entity.Transaction;
import com.github.seecret1.transaction_service.entity.enums.TransactionStatus;
import com.github.seecret1.transaction_service.utils.DetermineTransactionType;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionMessage message, String paymentId) {
        return Transaction.builder()
                .paymentId(paymentId)
                .userId(message.getUserId())
                .status(TransactionStatus.PENDING)
                .transactionType(DetermineTransactionType.getTransactionType(message.getPaymentType()))
                .sourceInvoiceId(message.getSourceInvoiceId())
                .destinationInvoiceId(message.getDestinationInvoiceId())
                .amount(message.getAmount())
                .currency(message.getCurrency())
                .build();
    }

    public TransactionDto toDto(Transaction entity) {
        return new TransactionDto(
                entity.getId(),
                entity.getUserId(),
                entity.getPaymentId(),
                entity.getSourceInvoiceId(),
                entity.getDestinationInvoiceId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getTransactionType(),
                entity.getStatus()
        );
    }
}

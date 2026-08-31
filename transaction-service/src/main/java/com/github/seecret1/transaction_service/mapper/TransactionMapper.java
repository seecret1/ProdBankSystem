package com.github.seecret1.transaction_service.mapper;

import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.dto.transaction.TransactionDto;
import com.github.seecret1.transaction_service.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionMessage message, String paymentId) {
        return Transaction.builder()
                .paymentId(paymentId)
                .build();
    }

    public TransactionDto toDto(Transaction entity) {
        return new TransactionDto(
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

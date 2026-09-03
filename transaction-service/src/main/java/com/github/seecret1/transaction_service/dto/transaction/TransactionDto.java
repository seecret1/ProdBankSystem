package com.github.seecret1.transaction_service.dto.transaction;

import com.github.seecret1.transaction_service.entity.enums.TransactionStatus;
import com.github.seecret1.transaction_service.entity.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionDto(

        String id,

        String userId,

        String paymentId,

        String sourceInvoiceId,

        String destinationInvoiceId,

        BigDecimal amount,

        String currency,

        TransactionType transactionType,

        TransactionStatus status

) { }

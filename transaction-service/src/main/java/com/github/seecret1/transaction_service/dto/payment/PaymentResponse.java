package com.github.seecret1.transaction_service.dto.payment;

import com.github.seecret1.transaction_service.entity.enums.PaymentType;

import java.math.BigDecimal;

public record PaymentResponse(

        String id,

        String sourceInvoiceId,

        String destinationInvoiceId,

        BigDecimal amount,

        PaymentType type,

        String currency

) { }

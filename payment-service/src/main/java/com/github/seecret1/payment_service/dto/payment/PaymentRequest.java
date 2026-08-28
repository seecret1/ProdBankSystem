package com.github.seecret1.payment_service.dto.payment;

import com.github.seecret1.payment_service.entity.enums.PaymentType;

import java.math.BigDecimal;

public record PaymentRequest(

        String sourceInvoiceId,

        String destinationInvoiceId,

        BigDecimal amount,

        PaymentType type,

        String currency,

        String comment

) { }

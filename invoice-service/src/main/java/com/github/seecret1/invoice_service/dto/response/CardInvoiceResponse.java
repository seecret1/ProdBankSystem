package com.github.seecret1.invoice_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.seecret1.invoice_service.entity.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record CardInvoiceResponse(

        String id,

        String cardId,

        String invoiceNumber,

        String currency,

        InvoiceStatus status,

        BigDecimal balance,

        String operationId,

        Boolean deleted,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant updatedAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant deletedAt,

        String deletedBy
) {
}

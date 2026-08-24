package com.github.seecret1.invoice_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.seecret1.invoice_service.entity.enums.OperationType;

import java.math.BigDecimal;
import java.time.Instant;

public record OperationResponse(

        String id,

        OperationType operationType,

        BigDecimal amountFrom,

        BigDecimal amountTo,

        BigDecimal commissionPercent,

        BigDecimal commissionAmount,

        Boolean isActive,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant updatedAt
) {
}

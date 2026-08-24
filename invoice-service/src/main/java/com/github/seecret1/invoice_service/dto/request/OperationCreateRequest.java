package com.github.seecret1.invoice_service.dto.request;

import com.github.seecret1.invoice_service.entity.enums.OperationType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record OperationCreateRequest(

        @NotNull(message = "operationType must be set")
        OperationType operationType,

        @NotNull(message = "amountFrom must be set")
        @Digits(integer = 18, fraction = 2, message = "amountFrom format invalid")
        BigDecimal amountFrom,

        @Digits(integer = 18, fraction = 2, message = "amountTo format invalid")
        BigDecimal amountTo,

        @NotNull(message = "commissionPercent must be set")
        @Digits(integer = 5, fraction = 2, message = "commissionPercent format invalid")
        @DecimalMin(value = "0.0", message = "commissionPercent must be >= 0")
        @DecimalMax(value = "100.00", message = "commissionPercent must be <= 100")
        BigDecimal commissionPercent,

        @Digits(integer = 18, fraction = 2, message = "commissionAmount format invalid")
        BigDecimal commissionAmount
) {
}

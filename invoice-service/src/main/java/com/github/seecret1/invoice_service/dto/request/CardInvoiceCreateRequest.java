package com.github.seecret1.invoice_service.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CardInvoiceCreateRequest(

        @NotBlank(message = "cardId must be set")
        @Size(max = 120, message = "cardId max size is 120")
        String cardId,

        @NotBlank(message = "invoiceNumber must be set")
        @Size(max = 50, message = "invoiceNumber max size is 50")
        String invoiceNumber,

        @NotBlank(message = "currency must be set")
        @Size(min = 3, max = 3, message = "currency must be 3 chars (ISO 4217)")
        String currency,

        @NotNull(message = "balance must be set")
        @Digits(integer = 18, fraction = 2, message = "balance format invalid")
        BigDecimal balance,

        @Size(max = 36, message = "operationId max size is 36")
        String operationId
) {
}

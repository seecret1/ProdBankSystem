package com.github.seecret1.cardservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardRequest (

    @NotBlank(message = "Card number must be set!")
    @Size(min = 16, max = 16, message = "The card number must contain 16 characters")
    String number,

    @NotNull(message = "Date activation must be set!")
    LocalDate dateActivation,

    @NotNull(message = "Date expiry must be set!")
    LocalDate dateExpiry,

    @PositiveOrZero
    BigDecimal balance,

    @NotBlank(message = "User must be set!")
    String userCriterial

) { }

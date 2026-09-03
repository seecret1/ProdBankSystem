package com.github.seecret1.cardservice.dto.request;

import com.github.seecret1.cardservice.dto.order.CardReceivingMethod;
import com.github.seecret1.cardservice.entity.enums.CardType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardRequest (

    @NotBlank(message = "Card number must be set!")
    @Size(min = 16, max = 16, message = "The card number must contain 16 characters")
    String number,

    @NotNull(message = "Card type must be set!")
    CardType type,

    @NotNull(message = "Date activation must be set!")
    LocalDate dateActivation,

    @NotNull(message = "Date expiry must be set!")
    LocalDate dateExpiry,

    String currency,

    @PositiveOrZero(message = "Balance can't be negative value!")
    BigDecimal balance,

    @NotNull(message = "Receiving method must be set!")
    CardReceivingMethod receivingMethod,

    CardDeliveryRequest cardDeliveryRequest,

    @Size(max = 255, message = "Max size message in {max} symbols")
    String comment

) { }

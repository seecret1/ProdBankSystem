package com.github.seecret1.cardservice.dto.request;

import com.github.seecret1.cardservice.entity.enums.CardStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStatusCardRequest (

    @NotBlank(message = "Card number must be set!")
    @Size(min = 16, max = 16, message = "The card number must contain 16 characters")
    String number,

    @NotNull(message = "Status must be set!")
    CardStatus status

) { }

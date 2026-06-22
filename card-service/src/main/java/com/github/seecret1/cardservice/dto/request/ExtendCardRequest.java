package com.github.seecret1.cardservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ExtendCardRequest(

    @NotBlank(message = "Card number must be set!")
    @Size(min = 16, max = 16, message = "The card number must contain 16 characters")
    String number,

    @NotBlank(message = "Card number must be set!")
    LocalDate dateExpiry

) { }

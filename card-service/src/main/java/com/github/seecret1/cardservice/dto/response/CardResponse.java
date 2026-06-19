package com.github.seecret1.cardservice.dto.response;

import com.github.seecret1.cardservice.entity.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse (

    String number,

    LocalDate dateActivation,

    LocalDate dateExpiry,

    CardStatus status,

    BigDecimal balance,

    String userId

) { }

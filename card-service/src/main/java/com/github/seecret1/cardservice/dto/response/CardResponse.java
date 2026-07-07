package com.github.seecret1.cardservice.dto.response;

import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.entity.enums.CardType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse (

    String number,

    CardType type,

    LocalDate dateActivation,

    LocalDate dateExpiry,

    CardStatus status,

    BigDecimal balance,

    BigDecimal spendingLimit,

    String userId

) { }

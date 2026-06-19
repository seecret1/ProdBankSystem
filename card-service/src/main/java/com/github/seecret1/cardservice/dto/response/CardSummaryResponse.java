package com.github.seecret1.cardservice.dto.response;

import com.github.seecret1.cardservice.entity.enums.CardStatus;

import java.math.BigDecimal;

public record CardSummaryResponse(

    String number,

    CardStatus status,

    BigDecimal balance

) { }

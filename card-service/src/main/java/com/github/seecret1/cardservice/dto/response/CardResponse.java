package com.github.seecret1.cardservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.entity.enums.CardType;

import java.time.LocalDate;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record CardResponse (

    String number,

    CardType type,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateActivation,

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateExpiry,

    CardStatus status,

    String userId

) { }

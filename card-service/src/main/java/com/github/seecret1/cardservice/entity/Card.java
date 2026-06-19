package com.github.seecret1.cardservice.entity;

import com.github.seecret1.cardservice.converter.CardNumberConverter;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "cards", schema = "card_bank")
@AllArgsConstructor
@NoArgsConstructor
public class Card extends BaseEntity {

    @Convert(converter = CardNumberConverter.class)
    @Column(columnDefinition = "TEXT")
    private String number;

    private String numberHash;

    private LocalDate dateActivation;

    private LocalDate dateExpiry;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private BigDecimal balance;

    @Column(name = "user_id", nullable = false)
    private String userId;
}

package com.github.seecret1.cardservice.entity;

import com.github.seecret1.cardservice.converter.CardNumberConverter;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.entity.enums.CardType;
import com.github.seecret1.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "cards", schema = "card_bank")
@AllArgsConstructor
@NoArgsConstructor
public class Card extends BaseEntity {

    @Convert(converter = CardNumberConverter.class)
    @Column(columnDefinition = "TEXT", unique = true, nullable = false)
    private String number;

    private String numberHash;

    @Enumerated(EnumType.STRING)
    private CardType type;

    private LocalDate dateActivation;

    private LocalDate dateExpiry;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private BigDecimal balance;

    private BigDecimal spendingLimit;

    @Column(name = "user_id", nullable = false)
    private String userId;

    public void softDelete(String author) {
        setDeleted(true);
        setDeletedAt(Instant.now());
        setDeletedBy(author);
    }
}

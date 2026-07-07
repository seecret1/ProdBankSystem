package com.github.seecret1.cardservice.order.message;

import com.github.seecret1.cardservice.entity.enums.CardType;
import jakarta.validation.ValidationException;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateCardDto extends OrderDto {

    private String cardId;

    private CardType cardType;

    private BigDecimal spendingLimit;

    private String comment;

    @Override
    public void validate() {
        if (cardId.isBlank()) {
            throw new ValidationException("Card id must not be blank!");
        }
        if (spendingLimit == null || spendingLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Spending limit must be positive for credit card");
        }
        if (spendingLimit.compareTo(BigDecimal.valueOf(100000)) > 0) {
            throw new ValidationException("Spending limit exceeds maximum allowed");
        }
    }
}

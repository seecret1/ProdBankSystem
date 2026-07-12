package com.github.seecret1.cardservice.order.message;

import com.github.seecret1.cardservice.entity.enums.CardType;
import jakarta.validation.ValidationException;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCardDto extends OrderDto {

    private String cardId;

    private CardType cardType;

    private BigDecimal spendingLimit;

    @Override
    public void validate() {
        if (userId == null || cardId == null || traceId == null) {
            throw new ValidationException("Ids must not be null");
        }
        if (userId.isBlank() || cardId.isBlank() || traceId.isBlank()) {
            throw new ValidationException("Ids must not be blank");
        }
        if (cardType == null || orderType == null || spendingLimit == null) {
            throw new ValidationException("Must not be null fields");
        }
    }
}

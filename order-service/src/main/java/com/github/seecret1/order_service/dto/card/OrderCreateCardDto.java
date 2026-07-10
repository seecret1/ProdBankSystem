package com.github.seecret1.order_service.dto.card;

import com.github.seecret1.order_service.dto.OrderDto;
import com.github.seecret1.order_service.entity.CardType;
import jakarta.validation.ValidationException;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateCardDto extends OrderDto {

    private String cardId;

    private CardType cardType;

    private BigDecimal spendingLimit;

    private String comment;
}

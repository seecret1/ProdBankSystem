package com.github.seecret1.order_service.dto.card;

import com.github.seecret1.order_service.entity.enums.CardReceivingMethod;
import com.github.seecret1.order_service.entity.enums.CardType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCardResponse {

    CardType cardType;

    BigDecimal spendingLimit;

    Instant createdAt;

    CardReceivingMethod cardReceivingMethod;
}

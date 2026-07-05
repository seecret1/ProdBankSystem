package com.github.seecret1.order_service.dto.card;

import com.github.seecret1.order_service.entity.CardType;
import com.github.seecret1.order_service.entity.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCardResponse {

    String traceId;

    UUID orderId;

    String userId;

    String cardId;

    CardType cardType;

    BigDecimal spendingLimit;

    boolean personal;

    String comment;

    OrderStatus status;

    Instant timestamp;
}

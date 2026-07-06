package com.github.seecret1.cardservice.order.message;

import com.github.seecret1.cardservice.entity.enums.CardType;
import com.github.seecret1.cardservice.order.OrderStatus;
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

    String traceId;

    String orderId;

    String userId;

    String cardId;

    CardType cardType;

    BigDecimal spendingLimit;

    boolean personal;

    String comment;

    OrderStatus status;

    Instant timestamp;
}

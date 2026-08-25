package com.github.seecret1.cardservice.dto.order.message;

import com.github.seecret1.cardservice.dto.order.CardReceivingMethod;
import com.github.seecret1.cardservice.entity.enums.CardType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCardResponse {

    CardType cardType;

    String invoiceId;

    Instant createdAt;

    CardReceivingMethod cardReceivingMethod;
}

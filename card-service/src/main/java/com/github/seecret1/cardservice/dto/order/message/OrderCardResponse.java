package com.github.seecret1.cardservice.dto.order.message;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCardResponse {

    String invoiceId;

    Instant createdAt;
}

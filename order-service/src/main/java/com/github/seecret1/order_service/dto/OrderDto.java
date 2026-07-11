package com.github.seecret1.order_service.dto;

import com.github.seecret1.order_service.entity.OrderType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class OrderDto {

    String traceId;

    String userId;

    OrderType orderType;

    Instant createdAt;

    String comment;

    public abstract void validate();
}

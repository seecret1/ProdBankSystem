package com.github.seecret1.order_service.dto;

import com.github.seecret1.order_service.entity.enums.OrderType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
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

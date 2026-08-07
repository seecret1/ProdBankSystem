package com.github.seecret1.delivery_service.dto.order;

import com.github.seecret1.delivery_service.entity.enums.OrderType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

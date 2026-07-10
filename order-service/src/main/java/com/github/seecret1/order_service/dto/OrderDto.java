package com.github.seecret1.order_service.dto;

import com.github.seecret1.order_service.entity.OrderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class OrderDto {

    private String traceId;

    private String userId;

    private OrderType orderType;

    private Instant createdAt;
}

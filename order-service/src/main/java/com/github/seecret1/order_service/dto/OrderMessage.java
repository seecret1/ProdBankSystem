package com.github.seecret1.order_service.dto;

import com.github.seecret1.order_service.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderMessage {

    private String traceId;

    private String orderId;

    private String userId;

    private String productId;

    private OrderStatus status;

    private Object data;

    private String message;

    private Instant timestamp;
}

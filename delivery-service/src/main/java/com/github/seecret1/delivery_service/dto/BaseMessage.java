package com.github.seecret1.delivery_service.dto;

import com.github.seecret1.delivery_service.entity.enums.OrderStatus;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseMessage {

    private String traceId;

    private String orderId;

    private String userId;

    private String productId;

    private OrderStatus status;

    private Object data;

    private String message;

    private Instant timestamp;
}

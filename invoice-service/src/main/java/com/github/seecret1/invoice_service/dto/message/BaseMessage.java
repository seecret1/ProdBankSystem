package com.github.seecret1.invoice_service.dto.message;

import com.github.seecret1.invoice_service.entity.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

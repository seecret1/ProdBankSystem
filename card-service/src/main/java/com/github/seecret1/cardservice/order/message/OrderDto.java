package com.github.seecret1.cardservice.order.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class OrderDto {

    private String traceId;

    private String orderId;

    private String userId;

    private OrderType orderType;

    private Instant createdAt;

    public abstract void validate();

    public enum OrderType {

        CARD,

        CREDIT
    }
}

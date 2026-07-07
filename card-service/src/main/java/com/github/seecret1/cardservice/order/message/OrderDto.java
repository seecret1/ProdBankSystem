package com.github.seecret1.cardservice.order.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class OrderDto {

    private String traceId;

    private String userId;

    private OrderType orderType;

    private Instant createdAt;

    public abstract void validate();

    public enum OrderType {

        CARD,

        CREDIT
    }
}

package com.github.seecret1.cardservice.order.message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

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

    public enum OrderType {

        CARD,

        CREDIT
    }
}

package com.github.seecret1.cardservice.order.message;

import com.github.seecret1.cardservice.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderMessage<T> {

    private String traceId;

    private String orderId;

    private String userId;

    private OrderStatus status;

    private T data;

    private String message;
}

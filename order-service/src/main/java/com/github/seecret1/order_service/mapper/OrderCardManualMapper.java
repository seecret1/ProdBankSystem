package com.github.seecret1.order_service.mapper;

import com.github.seecret1.order_service.dto.OrderMessage;
import com.github.seecret1.order_service.dto.card.OrderCardResponse;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.OrderStatus;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderCardManualMapper {

    public static OrderCard toEntity(OrderCardDto event) {
        return OrderCard.builder()
                .traceId(event.getTraceId())
                .userId(event.getUserId())
                .cardId(event.getCardId())
                .cardType(event.getCardType())
                .status(OrderStatus.PENDING)
                .comment(event.getComment())
                .requestTimestamp(event.getCreatedAt())
                .deleted(false)
                .build();
    }

    private static OrderCardResponse toResponse(OrderCard order) {
        return OrderCardResponse.builder()
                .cardId(order.getCardId())
                .cardType(order.getCardType())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public static OrderMessage<OrderCardResponse> toMessage(OrderCard order) {
        OrderMessage<OrderCardResponse> message = new OrderMessage<>();
        message.setTraceId(order.getTraceId());
        message.setUserId(order.getUserId());
        message.setOrderId(order.getId());
        message.setStatus(order.getStatus());
        message.setData(toResponse(order));
        message.setMessage(order.getComment());
        message.setTimestamp(order.getRequestTimestamp());
        return message;
    }
}

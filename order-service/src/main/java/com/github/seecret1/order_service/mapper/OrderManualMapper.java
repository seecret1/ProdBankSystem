package com.github.seecret1.order_service.mapper;

import com.github.seecret1.order_service.dto.card.OrderCardResponse;
import com.github.seecret1.order_service.dto.card.OrderCreateCardDto;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.OrderStatus;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderManualMapper {

    public static OrderCard toEntity(OrderCreateCardDto event) {
        return OrderCard.builder()
                .traceId(event.getTraceId())
                .userId(event.getUserId())
                .cardId(event.getCardId())
                .cardType(event.getCardType())
                .spendingLimit(event.getSpendingLimit())
                .comment(event.getComment())
                .status(OrderStatus.PENDING)
                .deleted(false)
                .build();
    }

    public static OrderCardResponse toResponse(OrderCard order) {
        return OrderCardResponse.builder()
                .traceId(order.getTraceId())
                .orderId(order.getId())
                .userId(order.getUserId())
                .cardId(order.getCardId())
                .cardType(order.getCardType())
                .spendingLimit(order.getSpendingLimit())
                .comment(order.getComment())
                .status(order.getStatus())
                .timestamp(order.getCreatedAt())
                .build();
    }
}

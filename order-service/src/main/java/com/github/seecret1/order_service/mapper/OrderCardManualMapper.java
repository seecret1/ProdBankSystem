package com.github.seecret1.order_service.mapper;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.card.OrderCardResponse;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.enums.OrderStatus;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderCardManualMapper {

    public static OrderCard toEntity(OrderCardDto event, OrderStatus status) {
        return OrderCard.builder()
                .traceId(event.getTraceId())
                .userId(event.getUserId())
                .cardId(event.getCardId())
                .spendingLimit(event.getSpendingLimit())
                .cardType(event.getCardType())
                .status(status)
                .cardReceivingMethod(event.getCardReceivingMethod())
                .comment(event.getComment())
                .requestTimestamp(event.getCreatedAt())
                .deleted(false)
                .build();
    }

    private static OrderCardResponse toResponse(OrderCard order) {
        return OrderCardResponse.builder()
                .cardType(order.getCardType())
                .spendingLimit(order.getSpendingLimit())
                .cardReceivingMethod(order.getCardReceivingMethod())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public static BaseMessage toMessage(OrderCard order) {
        BaseMessage message = new BaseMessage();
        message.setTraceId(order.getTraceId());
        message.setUserId(order.getUserId());
        message.setOrderId(order.getId());
        message.setProductId(order.getCardId());
        message.setStatus(order.getStatus());
        message.setData(toResponse(order));
        message.setMessage(order.getComment());
        message.setTimestamp(order.getRequestTimestamp());
        return message;
    }
}

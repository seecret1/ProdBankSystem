package com.github.seecret1.order_service.mapper;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.CardDeliveryRequest;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.card.OrderCardProcessingMessage;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.OrderCardDelivery;
import com.github.seecret1.order_service.entity.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCardManualMapper {

    private final AddressMapper addressMapper;

    public OrderCard toEntity(OrderCardDto event, OrderStatus status) {
        return OrderCard.builder()
                .traceId(event.getTraceId())
                .userId(event.getUserId())
                .cardId(event.getCardId())
                .invoiceId(event.getInvoiceId())
                .cardType(event.getCardType())
                .status(status)
                .cardReceivingMethod(event.getCardReceivingMethod())
                .comment(event.getComment())
                .requestTimestamp(event.getCreatedAt())
                .deleted(false)
                .build();
    }

    public OrderCardDto toDto(OrderCard order) {
        var dto = new OrderCardDto();
        dto.setTraceId(order.getTraceId());
        dto.setUserId(order.getUserId());
        dto.setCardId(order.getCardId());
        dto.setInvoiceId(order.getInvoiceId());
        dto.setCardType(order.getCardType());
        dto.setCardReceivingMethod(order.getCardReceivingMethod());
        dto.setComment(order.getComment());
        dto.setCreatedAt(order.getRequestTimestamp());
        dto.setDeliveryRequest(toCardDeliveryRequest(order.getOrderCardDelivery()));
        return dto;
    }

    private CardDeliveryRequest toCardDeliveryRequest(OrderCardDelivery delivery) {
        return new CardDeliveryRequest(
                delivery.getPlannedDeliveryTime(),
                addressMapper.toAddressRequestFromEntity(delivery.getAddress())
        );
    }

    private OrderCardProcessingMessage toResponse(OrderCard order) {
        return OrderCardProcessingMessage.builder()
                .cardType(order.getCardType())
                .cardReceivingMethod(order.getCardReceivingMethod())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public BaseMessage toMessage(OrderCard order) {
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
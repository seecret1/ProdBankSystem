package com.github.seecret1.order_service.mapper;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.CardDeliveryRequest;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.card.OrderCardResponse;
import com.github.seecret1.order_service.dto.invoice.OrderInvoiceDto;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.OrderCardDelivery;
import com.github.seecret1.order_service.entity.enums.OrderStatus;
import com.github.seecret1.order_service.entity.enums.OrderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OrderCardManualMapper {

    private final AddressManualMapper addressMapper;

    public OrderCard toEntity(OrderCardDto event, OrderStatus status) {
        OrderCardDelivery delivery = null;
        if (event.getDeliveryRequest() != null) {
            delivery = toOrderCardDelivery(event.getDeliveryRequest());
        }
        return OrderCard.builder()
                .traceId(event.getTraceId())
                .userId(event.getUserId())
                .cardId(event.getCardId())
                .cardType(event.getCardType())
                .status(status)
                .cardReceivingMethod(event.getCardReceivingMethod())
                .orderCardDelivery(delivery)
                .comment(event.getComment())
                .requestTimestamp(event.getCreatedAt())
                .balance(event.getBalance())
                .currency(event.getCurrency())
                .deleted(false)
                .build();
    }

    public OrderCardDelivery toOrderCardDelivery(CardDeliveryRequest deliveryRequest) {
        return OrderCardDelivery.builder()
                .plannedDeliveryTime(deliveryRequest.plannedDeliveryTime())
                .address(addressMapper.toAddress(deliveryRequest.address()))
                .build();
    }

    public OrderInvoiceDto toInvoiceDto(OrderCardDto event, String orderId) {
        return OrderInvoiceDto.builder()
                .traceId(event.getTraceId())
                .userId(event.getUserId())
                .cardId(event.getCardId())
                .cardType(event.getCardType())
                .orderId(orderId)
                .orderType(event.getOrderType())
                .currency(event.getCurrency())
                .balance(event.getBalance())
                .comment(event.getComment())
                .createdAt(Instant.now())
                .build();
    }

    public OrderCardDto toDto(OrderCard order) {
        var dto = new OrderCardDto();
        dto.setTraceId(order.getTraceId());
        dto.setUserId(order.getUserId());
        dto.setCardId(order.getCardId());
        dto.setCardType(order.getCardType());
        dto.setOrderType(OrderType.CARD);
        dto.setCardReceivingMethod(order.getCardReceivingMethod());
        dto.setComment(order.getComment());
        dto.setCurrency(order.getCurrency());
        dto.setBalance(order.getBalance());
        dto.setCreatedAt(order.getRequestTimestamp());
        if (order.getOrderCardDelivery() != null){
            dto.setDeliveryRequest(toCardDeliveryRequest(order.getOrderCardDelivery()));
        }
        return dto;
    }

    private CardDeliveryRequest toCardDeliveryRequest(OrderCardDelivery delivery) {
        return new CardDeliveryRequest(
                delivery.getPlannedDeliveryTime(),
                addressMapper.toAddressRequestFromEntity(delivery.getAddress())
        );
    }

    private OrderCardResponse toResponse(OrderCard order) {
        return OrderCardResponse.builder()
                .cardType(order.getCardType())
                .invoiceId(order.getInvoiceId())
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
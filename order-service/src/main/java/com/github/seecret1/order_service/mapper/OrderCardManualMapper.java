package com.github.seecret1.order_service.mapper;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.CardDeliveryRequest;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.card.OrderCardResponse;
import com.github.seecret1.order_service.dto.delivery.OrderCardDeliveryDto;
import com.github.seecret1.order_service.dto.invoice.OrderInvoiceDto;
import com.github.seecret1.order_service.dto.user.FullNameDto;
import com.github.seecret1.order_service.entity.FullName;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.OrderDelivery;
import com.github.seecret1.order_service.entity.enums.OrderStatus;
import com.github.seecret1.order_service.entity.enums.OrderType;
import com.github.seecret1.order_service.entity.enums.PersonType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OrderCardManualMapper {

    private final AddressManualMapper addressMapper;

    public OrderCard toEntity(OrderCardDto event, OrderStatus status) {
        return OrderCard.builder()
                .traceId(event.getTraceId())
                .userId(event.getUserId())
                .cardId(event.getCardId())
                .cardType(event.getCardType())
                .status(status)
                .cardReceivingMethod(event.getCardReceivingMethod())
                .orderDelivery(toCardDelivery(event.getDeliveryRequest()))
                .comment(event.getComment())
                .requestTimestamp(event.getCreatedAt())
                .balance(event.getBalance())
                .currency(event.getCurrency())
                .deleted(false)
                .build();
    }

    public OrderDelivery toOrderCardDelivery(
            OrderCardDeliveryDto dto
    ) {
        return OrderDelivery.builder()
                .plannedDeliveryTime(dto.getPlannedDeliveryTime())
                .originalAddress(addressMapper.toAddress(dto.getOriginAddress()))
                .destinationAddress(addressMapper.toAddress(dto.getDestinationAddress()))
                .fullName(toFullName(dto.getFullName()))
                .contactPhone(dto.getContactPhone())
                .personType(dto.getPersonType())
                .officeId(dto.getOfficeId())
                .build();
    }

    private FullName toFullName(FullNameDto dto) {
        return new FullName(dto.getFirstName(), dto.getLastName(), dto.getMiddleName());
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
        if (order.getOrderDelivery() != null){
            dto.setDeliveryRequest(toCardDeliveryRequest(order.getOrderDelivery()));
        }
        return dto;
    }

    private OrderDelivery toCardDelivery(CardDeliveryRequest delivery) {
        return OrderDelivery.builder()
                .plannedDeliveryTime(delivery.plannedDeliveryTime())
                .destinationAddress(addressMapper.toAddress(delivery.address()))
                .personType(PersonType.PHYSICAL)
                .build();
    }

    private CardDeliveryRequest toCardDeliveryRequest(OrderDelivery delivery) {
        return new CardDeliveryRequest(
                delivery.getPlannedDeliveryTime(),
                addressMapper.toAddressRequestFromEntity(delivery.getDestinationAddress())
        );
    }

    public OrderCardResponse toResponse(OrderCard order) {
        return OrderCardResponse.builder()
                .invoiceId(order.getInvoiceId())
                .createdAt(Instant.now())
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
package com.github.seecret1.order_service.mapper;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.card.OrderCardResponse;
import com.github.seecret1.order_service.entity.OrderCard;
import com.github.seecret1.order_service.entity.enums.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(
        componentModel = SPRING,
        injectionStrategy = CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderCardMapper {

    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "status", expression = "java(status)")
    OrderCard toEntity(OrderCardDto event, OrderStatus status);

    @Named("toResponse")
    OrderCardResponse toResponse(OrderCard order);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "productId", source = "cardId")
    @Mapping(target = "timestamp", source = "requestTimestamp")
    @Mapping(target = "data", qualifiedByName = "toResponse")
    @Mapping(target = "message", source = "comment")
    BaseMessage toMessage(OrderCard order);
}

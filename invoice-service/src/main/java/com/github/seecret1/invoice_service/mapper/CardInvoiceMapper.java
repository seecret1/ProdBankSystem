package com.github.seecret1.invoice_service.mapper;

import com.github.seecret1.invoice_service.dto.message.BaseMessage;
import com.github.seecret1.invoice_service.dto.order.OrderInvoiceDto;
import com.github.seecret1.invoice_service.dto.request.CardInvoiceCreateRequest;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.enums.InvoiceStatus;
import com.github.seecret1.invoice_service.entity.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CardInvoiceMapper {

    public CardInvoiceResponse toResponse(CardInvoice entity) {
        return new CardInvoiceResponse(
                entity.getId(),
                entity.getCardId(),
                entity.getCardType(),
                entity.getInvoiceNumber(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getBalance(),
                entity.getDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy(),
                entity.getSpendingLimit(),
                entity.getFreeLimit()
        );
    }

    public List<CardInvoiceResponse> toResponseList(List<CardInvoice> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CardInvoice toEntity(CardInvoiceCreateRequest request) {
        return CardInvoice.builder()
                .cardId(request.cardId())
                .cardType(request.cardType())
                .userId(request.userId())
                .invoiceNumber(request.invoiceNumber())
                .currency(request.currency())
                .status(InvoiceStatus.ACTIVE)
                .balance(request.balance())
                .spendingLimit(request.spendingLimit())
                .freeLimit(request.freeLimit())
                .deleted(false)
                .build();
    }

    public BaseMessage toMessageList(
            OrderInvoiceDto dto,
            List<CardInvoiceResponse> responses,
            OrderStatus orderStatus,
            String message
    ) {
        return BaseMessage.builder()
                .traceId(dto.getTraceId())
                .userId(dto.getUserId())
                .orderId(dto.getOrderId())
                .productId(dto.getCardId())
                .status(orderStatus)
                .data(responses)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public BaseMessage toMessage(
            OrderInvoiceDto dto,
            CardInvoiceResponse invoice,
            OrderStatus orderStatus,
            String message
    ) {
        return BaseMessage.builder()
                .traceId(dto.getTraceId())
                .userId(dto.getUserId())
                .productId(invoice.id())
                .orderId(dto.getOrderId())
                .status(orderStatus)
                .data(invoice)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}

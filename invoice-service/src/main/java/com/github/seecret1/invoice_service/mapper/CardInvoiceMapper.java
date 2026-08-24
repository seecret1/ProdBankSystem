package com.github.seecret1.invoice_service.mapper;

import com.github.seecret1.invoice_service.dto.order.BaseMessage;
import com.github.seecret1.invoice_service.dto.order.OrderCardDto;
import com.github.seecret1.invoice_service.dto.request.CardInvoiceCreateRequest;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.enums.InvoiceStatus;
import com.github.seecret1.invoice_service.entity.Operation;
import com.github.seecret1.invoice_service.entity.enums.OrderStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CardInvoiceMapper {

    public CardInvoiceResponse toResponse(CardInvoice entity) {
        String operationId = entity.getOperation() != null ? entity.getOperation().getId() : null;
        return new CardInvoiceResponse(
                entity.getId(),
                entity.getCardId(),
                entity.getInvoiceNumber(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getBalance(),
                operationId,
                entity.getDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getDeletedBy()
        );
    }

    public List<CardInvoiceResponse> toResponseList(List<CardInvoice> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CardInvoice toEntity(CardInvoiceCreateRequest request, Operation operation) {
        return CardInvoice.builder()
                .cardId(request.cardId())
                .invoiceNumber(request.invoiceNumber())
                .currency(request.currency())
                .status(InvoiceStatus.ACTIVE)
                .balance(request.balance())
                .operation(operation)
                .deleted(false)
                .build();
    }

    public BaseMessage toMessage(
            OrderCardDto dto,
            CardInvoiceResponse response,
            OrderStatus orderStatus,
            String message
    ) {
        return BaseMessage.builder()
                .traceId(dto.getTraceId())
                .userId(dto.getUserId())
                .productId(response.id())
                .status(orderStatus)
                .data(response)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}

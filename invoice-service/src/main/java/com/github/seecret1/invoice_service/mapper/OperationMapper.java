package com.github.seecret1.invoice_service.mapper;

import com.github.seecret1.invoice_service.dto.request.OperationCreateRequest;
import com.github.seecret1.invoice_service.dto.response.OperationResponse;
import com.github.seecret1.invoice_service.entity.Operation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OperationMapper {

    public OperationResponse toResponse(Operation entity) {
        return new OperationResponse(
                entity.getId(),
                entity.getOperationType(),
                entity.getAmount(),
                entity.getFullAmount(),
                entity.getCommissionPercent(),
                entity.getCommissionAmount(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public List<OperationResponse> toResponseList(List<Operation> entities) {
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Operation toEntity(OperationCreateRequest request) {
        return Operation.builder()
                .operationType(request.operationType())
                .amount(request.amount())
                .fullAmount(request.fullAmount())
                .commissionPercent(request.commissionPercent())
                .commissionAmount(request.commissionAmount() != null ? request.commissionAmount() : java.math.BigDecimal.ZERO)
                .isActive(true)
                .build();
    }
}

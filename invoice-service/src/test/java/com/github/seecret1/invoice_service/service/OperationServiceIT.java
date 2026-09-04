package com.github.seecret1.invoice_service.service;

import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.invoice_service.AbstractIntegrationTest;
import com.github.seecret1.invoice_service.dto.request.OperationCreateRequest;
import com.github.seecret1.invoice_service.dto.response.OperationResponse;
import com.github.seecret1.invoice_service.entity.enums.OperationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OperationService Integration Tests (H2)")
class OperationServiceIT extends AbstractIntegrationTest {

    @Autowired private OperationService operationService;

    @Test @DisplayName("create, findById, softDelete, hardDelete lifecycle")
    void shouldLifecycle() {
        var req = new OperationCreateRequest(OperationType.PAYMENT, new BigDecimal("100.00"), new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        OperationResponse created = operationService.create(req);
        assertThat(created.id()).isNotNull();
        assertThat(created.operationType()).isEqualTo(OperationType.PAYMENT);

        OperationResponse found = operationService.findById(created.id());
        assertThat(found.id()).isEqualTo(created.id());

        var page = operationService.findAll(new PageModel(0, 10));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);

        operationService.softDelete(created.id());
        assertThatThrownBy(() -> operationService.findById(created.id()))
                .isInstanceOf(com.github.seecret1.invoice_service.exception.OperationNotFoundException.class);

        var allIncluding = operationService.findAllIncludingInactive(new PageModel(0, 10));
        assertThat(allIncluding.getTotalElements()).isGreaterThanOrEqualTo(1);

        operationService.hardDelete(created.id());
        assertThatThrownBy(() -> operationService.hardDelete(created.id()))
                .isInstanceOf(com.github.seecret1.invoice_service.exception.OperationNotFoundException.class);
    }

    @Test @DisplayName("softDelete: should throw OperationAlreadyDeletedException on double soft delete")
    void shouldThrowAlreadyDeleted() {
        var req = new OperationCreateRequest(OperationType.DEPOSIT, new BigDecimal("50.00"), new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        OperationResponse created = operationService.create(req);
        operationService.softDelete(created.id());
        assertThatThrownBy(() -> operationService.softDelete(created.id()))
                .isInstanceOf(com.github.seecret1.invoice_service.exception.OperationAlreadyDeletedException.class);
    }
}

package com.github.seecret1.invoice_service.repository;

import com.github.seecret1.invoice_service.AbstractIntegrationTest;
import com.github.seecret1.invoice_service.entity.Operation;
import com.github.seecret1.invoice_service.entity.enums.OperationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OperationRepository Integration Tests (H2)")
class OperationRepositoryIT extends AbstractIntegrationTest {

    @Autowired private OperationRepository repository;

    private Operation newOp(OperationType type) {
        return Operation.builder().operationType(type).amount(new BigDecimal("100.00")).fullAmount(new BigDecimal("100.00")).commissionPercent(BigDecimal.ZERO).commissionAmount(BigDecimal.ZERO).isActive(true).build();
    }

    @Test @DisplayName("should save and find active")
    void shouldSaveAndFindActive() {
        Operation op = repository.save(newOp(OperationType.PAYMENT));
        assertThat(repository.findByIdActive(op.getId())).isPresent();
        assertThat(repository.findByIdIncludingInactive(op.getId())).isPresent();
    }

    @Test @DisplayName("should not find inactive via findByIdActive")
    void shouldNotFindInactive() {
        Operation op = repository.save(newOp(OperationType.DEPOSIT));
        op.setIsActive(false);
        repository.save(op);
        assertThat(repository.findByIdActive(op.getId())).isEmpty();
        assertThat(repository.findByIdIncludingInactive(op.getId())).isPresent();
    }

    @Test @DisplayName("should find for update with lock")
    void shouldFindForUpdate() {
        Operation op = repository.save(newOp(OperationType.WITHDRAWAL));
        assertThat(repository.findByIdForUpdateActive(op.getId())).isPresent();
        op.setIsActive(false);
        repository.save(op);
        assertThat(repository.findByIdForUpdateActive(op.getId())).isEmpty();
    }

    @Test @DisplayName("should paginate active")
    void shouldPaginate() {
        repository.save(newOp(OperationType.PAYMENT));
        repository.save(newOp(OperationType.REFUND));
        var page = repository.findAllActive(org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    }
}

package com.github.seecret1.invoice_service.mapper;

import com.github.seecret1.invoice_service.dto.request.OperationCreateRequest;
import com.github.seecret1.invoice_service.dto.response.OperationResponse;
import com.github.seecret1.invoice_service.entity.Operation;
import com.github.seecret1.invoice_service.entity.enums.OperationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OperationMapper Unit Tests")
class OperationMapperTest {

    private final OperationMapper mapper = new OperationMapper();

    @Test @DisplayName("toResponse: should map entity")
    void shouldMapToResponse() {
        Operation op = Operation.builder().id("id1").operationType(OperationType.PAYMENT).amount(new BigDecimal("100.00")).fullAmount(new BigDecimal("105.00")).commissionPercent(new BigDecimal("5.00")).commissionAmount(new BigDecimal("5.00")).isActive(true).build();
        OperationResponse r = mapper.toResponse(op);
        assertThat(r.id()).isEqualTo("id1");
        assertThat(r.operationType()).isEqualTo(OperationType.PAYMENT);
        assertThat(r.commissionAmount()).isEqualByComparingTo("5.00");
    }

    @Test @DisplayName("toResponseList: should map list")
    void shouldMapList() {
        Operation o1 = Operation.builder().id("1").operationType(OperationType.DEPOSIT).amount(BigDecimal.TEN).fullAmount(BigDecimal.TEN).commissionPercent(BigDecimal.ZERO).commissionAmount(BigDecimal.ZERO).isActive(true).build();
        Operation o2 = Operation.builder().id("2").operationType(OperationType.WITHDRAWAL).amount(BigDecimal.ONE).fullAmount(BigDecimal.ONE).commissionPercent(BigDecimal.ZERO).commissionAmount(BigDecimal.ONE).isActive(false).build();
        List<OperationResponse> list = mapper.toResponseList(List.of(o1, o2));
        assertThat(list).hasSize(2);
        assertThat(list.get(1).isActive()).isFalse();
    }

    @Test @DisplayName("toEntity: should map request with defaults")
    void shouldMapToEntity() {
        var req = new OperationCreateRequest(OperationType.COMMISSION, new BigDecimal("50.00"), new BigDecimal("55.00"), new BigDecimal("10.00"), new BigDecimal("5.00"));
        Operation e = mapper.toEntity(req);
        assertThat(e.getOperationType()).isEqualTo(OperationType.COMMISSION);
        assertThat(e.getIsActive()).isTrue();
        assertThat(e.getAmount()).isEqualByComparingTo("50.00");
    }

    @Test @DisplayName("toEntity: should default commissionAmount to ZERO when null")
    void shouldDefaultCommission() {
        var req = new OperationCreateRequest(OperationType.PAYMENT, new BigDecimal("100.00"), new BigDecimal("100.00"), new BigDecimal("0.00"), null);
        Operation e = mapper.toEntity(req);
        assertThat(e.getCommissionAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

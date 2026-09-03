package com.github.seecret1.invoice_service.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.invoice_service.dto.request.OperationCreateRequest;
import com.github.seecret1.invoice_service.dto.response.OperationResponse;
import com.github.seecret1.invoice_service.entity.Operation;
import com.github.seecret1.invoice_service.entity.enums.OperationType;
import com.github.seecret1.invoice_service.exception.OperationAlreadyDeletedException;
import com.github.seecret1.invoice_service.exception.OperationNotFoundException;
import com.github.seecret1.invoice_service.mapper.OperationMapper;
import com.github.seecret1.invoice_service.repository.CardInvoiceRepository;
import com.github.seecret1.invoice_service.repository.OperationRepository;
import com.github.seecret1.invoice_service.service.impl.OperationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OperationServiceImpl Unit Tests")
class OperationServiceImplTest {

    @Mock private CardInvoiceRepository cardInvoiceRepository;
    @Mock private OperationRepository operationRepository;
    @Mock private OperationMapper operationMapper;

    @InjectMocks private OperationServiceImpl service;

    private Operation operation;
    private OperationResponse response;

    @BeforeEach
    void setUp() {
        operation = Operation.builder().id("op-1").operationType(OperationType.PAYMENT).amount(new BigDecimal("100.00")).fullAmount(new BigDecimal("100.00")).commissionPercent(BigDecimal.ZERO).commissionAmount(BigDecimal.ZERO).isActive(true).build();
        response = new OperationResponse("op-1", OperationType.PAYMENT, new BigDecimal("100.00"), new BigDecimal("100.00"), BigDecimal.ZERO, BigDecimal.ZERO, true, null, null);
    }

    @Test @DisplayName("findById: should return response when found")
    void shouldFindById() {
        when(operationRepository.findByIdActive("op-1")).thenReturn(Optional.of(operation));
        when(operationMapper.toResponse(operation)).thenReturn(response);
        OperationResponse result = service.findById("op-1");
        assertThat(result).isEqualTo(response);
    }

    @Test @DisplayName("findById: should throw OperationNotFoundException when not found")
    void shouldThrowWhenNotFound() {
        when(operationRepository.findByIdActive("op-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById("op-1")).isInstanceOf(OperationNotFoundException.class);
    }

    @Test @DisplayName("findAll: should return page")
    void shouldFindAll() {
        Page<Operation> page = new PageImpl<>(List.of(operation));
        when(operationRepository.findAllActive(any(Pageable.class))).thenReturn(page);
        when(operationMapper.toResponseList(anyList())).thenReturn(List.of(response));
        PageResponse<OperationResponse> result = service.findAll(new PageModel(0, 10));
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test @DisplayName("create: should save and return")
    void shouldCreate() {
        var req = new OperationCreateRequest(OperationType.DEPOSIT, new BigDecimal("50.00"), new BigDecimal("50.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        when(operationMapper.toEntity(req)).thenReturn(operation);
        when(operationRepository.save(operation)).thenReturn(operation);
        when(operationMapper.toResponse(operation)).thenReturn(response);
        OperationResponse result = service.create(req);
        assertThat(result).isEqualTo(response);
        verify(operationRepository).save(operation);
    }

    @Test @DisplayName("softDelete: should set isActive false")
    void shouldSoftDelete() {
        when(operationRepository.findByIdForUpdateActive("op-1")).thenReturn(Optional.of(operation));
        service.softDelete("op-1");
        assertThat(operation.getIsActive()).isFalse();
        verify(operationRepository).save(operation);
    }

    @Test @DisplayName("softDelete: should throw OperationAlreadyDeletedException when already deleted")
    void shouldThrowAlreadyDeleted() {
        Operation inactive = Operation.builder().id("op-1").isActive(false).build();
        when(operationRepository.findByIdForUpdateActive("op-1")).thenReturn(Optional.empty());
        when(operationRepository.findByIdIncludingInactive("op-1")).thenReturn(Optional.of(inactive));
        assertThatThrownBy(() -> service.softDelete("op-1")).isInstanceOf(OperationAlreadyDeletedException.class);
    }

    @Test @DisplayName("softDelete: should throw OperationNotFoundException when not exists")
    void shouldThrowNotFoundOnSoftDelete() {
        when(operationRepository.findByIdForUpdateActive("op-1")).thenReturn(Optional.empty());
        when(operationRepository.findByIdIncludingInactive("op-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.softDelete("op-1")).isInstanceOf(OperationNotFoundException.class);
    }

    @Test @DisplayName("hardDelete: should delete")
    void shouldHardDelete() {
        when(operationRepository.findByIdIncludingInactive("op-1")).thenReturn(Optional.of(operation));
        service.hardDelete("op-1");
        verify(operationRepository).delete(operation);
    }

    @Test @DisplayName("hardDelete: should throw when not found")
    void shouldThrowOnHardDeleteNotFound() {
        when(operationRepository.findByIdIncludingInactive("op-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.hardDelete("op-1")).isInstanceOf(OperationNotFoundException.class);
    }
}

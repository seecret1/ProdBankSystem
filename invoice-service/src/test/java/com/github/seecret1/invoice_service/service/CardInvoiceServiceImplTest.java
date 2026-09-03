package com.github.seecret1.invoice_service.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.invoice_service.dto.request.CardInvoiceCreateRequest;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.enums.InvoiceStatus;
import com.github.seecret1.invoice_service.exception.InvoiceAlreadyDeletedException;
import com.github.seecret1.invoice_service.exception.InvoiceAlreadyExistsException;
import com.github.seecret1.invoice_service.exception.InvoiceNotFoundException;
import com.github.seecret1.invoice_service.mapper.CardInvoiceMapper;
import com.github.seecret1.invoice_service.mapper.OperationMapper;
import com.github.seecret1.invoice_service.repository.CardInvoiceRepository;
import com.github.seecret1.invoice_service.service.impl.CardInvoiceServiceImpl;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardInvoiceServiceImpl Unit Tests")
class CardInvoiceServiceImplTest {

    @Mock private CardInvoiceRepository cardInvoiceRepository;
    @Mock private CardInvoiceMapper cardInvoiceMapper;
    @Mock private OperationMapper operationMapper;

    @InjectMocks private CardInvoiceServiceImpl service;

    private CardInvoice invoice;
    private CardInvoiceResponse response;
    private CardInvoiceCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "internalApiKey", "test-key");
        invoice = CardInvoice.builder().id("inv-1").cardId("card-1").invoiceNumber("INV-1").userId("user-1").currency("RUB").status(InvoiceStatus.ACTIVE).balance(new BigDecimal("1000.00")).spendingLimit(new BigDecimal("200000")).freeLimit(new BigDecimal("100000")).deleted(false).build();
        response = new CardInvoiceResponse("inv-1", "card-1", "INV-1", "RUB", InvoiceStatus.ACTIVE, new BigDecimal("1000.00"), false, null, null, null, null, new BigDecimal("200000"), new BigDecimal("100000"));
        createRequest = new CardInvoiceCreateRequest("card-1", "user-1", "INV-1", "RUB", new BigDecimal("1000.00"), new BigDecimal("200000"), new BigDecimal("100000"));
    }

    @Test @DisplayName("findById: should return when not deleted")
    void shouldFindById() {
        when(cardInvoiceRepository.findByIdNotDeleted("inv-1")).thenReturn(Optional.of(invoice));
        when(cardInvoiceMapper.toResponse(invoice)).thenReturn(response);
        assertThat(service.findById("inv-1")).isEqualTo(response);
    }

    @Test @DisplayName("findById: should throw InvoiceNotFoundException when not found")
    void shouldThrowWhenNotFound() {
        when(cardInvoiceRepository.findByIdNotDeleted("inv-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById("inv-1")).isInstanceOf(InvoiceNotFoundException.class);
    }

    @Test @DisplayName("findByCardId: should return invoice")
    void shouldFindByCardId() {
        when(cardInvoiceRepository.findByCardId("card-1")).thenReturn(Optional.of(invoice));
        when(cardInvoiceMapper.toResponse(invoice)).thenReturn(response);
        assertThat(service.findByCardId("card-1")).isEqualTo(response);
    }

    @Test @DisplayName("findByUserId: should return list")
    void shouldFindByUserId() {
        when(cardInvoiceRepository.findByUserId("user-1")).thenReturn(List.of(invoice));
        when(cardInvoiceMapper.toResponseList(List.of(invoice))).thenReturn(List.of(response));
        assertThat(service.findByUserId("user-1")).hasSize(1);
    }

    @Test @DisplayName("findAll: should return page")
    void shouldFindAll() {
        Page<CardInvoice> page = new PageImpl<>(List.of(invoice));
        when(cardInvoiceRepository.findAllNotDeleted(any(Pageable.class))).thenReturn(page);
        when(cardInvoiceMapper.toResponseList(anyList())).thenReturn(List.of(response));
        PageResponse<CardInvoiceResponse> result = service.findAll(new PageModel(0, 10));
        assertThat(result.getData()).hasSize(1);
    }

    @Test @DisplayName("create: should save when invoiceNumber and cardId unique")
    void shouldCreate() {
        when(cardInvoiceRepository.existsByInvoiceNumber("INV-1")).thenReturn(false);
        when(cardInvoiceRepository.existsByCardId("card-1")).thenReturn(false);
        when(cardInvoiceMapper.toEntity(createRequest)).thenReturn(invoice);
        when(cardInvoiceRepository.save(invoice)).thenReturn(invoice);
        when(cardInvoiceMapper.toResponse(invoice)).thenReturn(response);
        assertThat(service.create(createRequest)).isEqualTo(response);
        verify(cardInvoiceRepository).save(invoice);
    }

    @Test @DisplayName("create: should throw InvoiceAlreadyExistsException when invoiceNumber exists")
    void shouldThrowWhenInvoiceExists() {
        when(cardInvoiceRepository.existsByInvoiceNumber("INV-1")).thenReturn(true);
        assertThatThrownBy(() -> service.create(createRequest)).isInstanceOf(InvoiceAlreadyExistsException.class);
    }

    @Test @DisplayName("create: should throw when cardId exists")
    void shouldThrowWhenCardExists() {
        when(cardInvoiceRepository.existsByInvoiceNumber("INV-1")).thenReturn(false);
        when(cardInvoiceRepository.existsByCardId("card-1")).thenReturn(true);
        assertThatThrownBy(() -> service.create(createRequest)).isInstanceOf(InvoiceAlreadyExistsException.class);
    }

    @Test @DisplayName("softDelete: should mark deleted")
    void shouldSoftDelete() {
        when(cardInvoiceRepository.findByIdForUpdate("inv-1")).thenReturn(Optional.of(invoice));
        service.softDelete("inv-1", "admin");
        assertThat(invoice.getDeleted()).isTrue();
        assertThat(invoice.getDeletedBy()).isEqualTo("admin");
        verify(cardInvoiceRepository).save(invoice);
    }

    @Test @DisplayName("softDelete: should throw InvoiceAlreadyDeletedException when already deleted")
    void shouldThrowAlreadyDeleted() {
        CardInvoice deleted = CardInvoice.builder().id("inv-1").deleted(true).deletedBy("someone").build();
        when(cardInvoiceRepository.findByIdForUpdate("inv-1")).thenReturn(Optional.empty());
        when(cardInvoiceRepository.findByIdIncludingDeleted("inv-1")).thenReturn(Optional.of(deleted));
        assertThatThrownBy(() -> service.softDelete("inv-1", "admin")).isInstanceOf(InvoiceAlreadyDeletedException.class);
    }

    @Test @DisplayName("hardDelete: should delete")
    void shouldHardDelete() {
        when(cardInvoiceRepository.findByIdIncludingDeleted("inv-1")).thenReturn(Optional.of(invoice));
        service.hardDelete("inv-1");
        verify(cardInvoiceRepository).delete(invoice);
    }

    @Test @DisplayName("hardDelete: should throw when not found")
    void shouldThrowOnHardDeleteNotFound() {
        when(cardInvoiceRepository.findByIdIncludingDeleted("inv-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.hardDelete("inv-1")).isInstanceOf(InvoiceNotFoundException.class);
    }

    @Test @DisplayName("softDelete: should use system when deletedBy blank")
    void shouldUseSystemWhenBlank() {
        when(cardInvoiceRepository.findByIdForUpdate("inv-1")).thenReturn(Optional.of(invoice));
        service.softDelete("inv-1", "   ");
        assertThat(invoice.getDeletedBy()).isEqualTo("system");
    }
}

package com.github.seecret1.invoice_service.service.impl;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.invoice_service.dto.request.CardInvoiceCreateRequest;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.dto.response.OperationResponse;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.Operation;
import com.github.seecret1.invoice_service.exception.InvoiceAlreadyDeletedException;
import com.github.seecret1.invoice_service.exception.InvoiceAlreadyExistsException;
import com.github.seecret1.invoice_service.exception.InvoiceNotFoundException;
import com.github.seecret1.invoice_service.mapper.CardInvoiceMapper;
import com.github.seecret1.invoice_service.mapper.OperationMapper;
import com.github.seecret1.invoice_service.repository.CardInvoiceRepository;
import com.github.seecret1.invoice_service.repository.OperationRepository;
import com.github.seecret1.invoice_service.service.CardInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardInvoiceServiceImpl implements CardInvoiceService {

    private final CardInvoiceRepository cardInvoiceRepository;

    private final OperationRepository operationRepository;

    private final CardInvoiceMapper cardInvoiceMapper;

    private final OperationMapper operationMapper;

    @Value("${services.api-key}")
    private String internalApiKey;

    @Override
    @Transactional(readOnly = true)
    public CardInvoiceResponse findById(String id) {
        log.info("Get invoice by id: {}", id);
        CardInvoice invoice = findNotDeletedById(id);
        log.debug("Found invoice: {}", invoice.getId());
        return cardInvoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CardInvoiceResponse> findAll(PageModel pageModel) {
        log.info("Get all invoices page: {}, size: {}", pageModel.getNumber(), pageModel.getSize());
        Pageable pageable = pageModel.toPageRequest();
        var page = cardInvoiceRepository.findAllNotDeleted(pageable);
        log.debug("Found invoices totalElements: {}, totalPages: {}", page.getTotalElements(), page.getTotalPages());
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                cardInvoiceMapper.toResponseList(page.getContent())
        );
    }

    @Override
    @Transactional(readOnly = true) //TODO: мб под снос
    public PageResponse<OperationResponse> findInvoiceOperations(String id, PageModel pageModel) {
        Pageable pageable = pageModel.toPageRequest();
        var page = cardInvoiceRepository.findAllOperationsInInvoice(id, pageable);
        log.debug("Found invoice operations: {}, size: {}", page, pageModel.getSize());
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                operationMapper.toResponseList(page.getContent())
        );
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CardInvoiceResponse create(CardInvoiceCreateRequest request) {
        log.info("Create invoice: cardId={}, invoiceNumber={}", request.cardId(), request.invoiceNumber());

        if (cardInvoiceRepository.existsByInvoiceNumber(request.invoiceNumber())) {
            throw new InvoiceAlreadyExistsException("Invoice already exists by invoiceNumber: " + request.invoiceNumber());
        }
        if (cardInvoiceRepository.existsByCardId(request.cardId())) {
            throw new InvoiceAlreadyExistsException("Invoice already exists by cardId: " + request.cardId());
        }

        Operation operation = null;
        if (request.operationId() != null && !request.operationId().isBlank()) {
            operation = operationRepository.findById(request.operationId())
                    .orElseThrow(() -> new InvoiceNotFoundException("Operation not found by id: " + request.operationId()));
        }

        CardInvoice entity = cardInvoiceMapper.toEntity(request, operation);
        CardInvoice saved = cardInvoiceRepository.save(entity);
        log.info("Invoice created with id: {}", saved.getId());
        return cardInvoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(String id, String deletedBy) {
        log.info("Soft delete invoice by id: {}", id);
        CardInvoice invoice = findNotDeletedByIdForUpdate(id);
        String author = (deletedBy != null && !deletedBy.isBlank()) ? deletedBy : "system";
        invoice.softDelete(author);
        cardInvoiceRepository.save(invoice);
        log.info("Soft delete successful for id: {} by {}", id, author);
    }

    @Override
    @Transactional
    public void hardDelete(String id) {
        log.info("Hard delete invoice by id: {}", id);
        CardInvoice invoice = cardInvoiceRepository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found by id: " + id));
        cardInvoiceRepository.delete(invoice);
        log.info("Hard delete successful for id: {}", id);
    }

    private CardInvoice findNotDeletedById(String id) {
        return cardInvoiceRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new InvoiceNotFoundException("Invoice not found by id: " + id));
    }

    private CardInvoice findNotDeletedByIdForUpdate(String id) {
        return cardInvoiceRepository.findByIdForUpdate(id)
                .orElseThrow(() -> {
                    var deletedOpt = cardInvoiceRepository.findByIdIncludingDeleted(id);
                    if (deletedOpt.isPresent() && Boolean.TRUE.equals(deletedOpt.get().getDeleted())) {
                        return new InvoiceAlreadyDeletedException(
                                "Invoice already soft-deleted by id: " + id + " by " + deletedOpt.get().getDeletedBy()
                        );
                    }
                    return new InvoiceNotFoundException("Invoice not found by id: " + id);
                });
    }
}

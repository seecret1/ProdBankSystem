package com.github.seecret1.invoice_service.service.impl;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.invoice_service.dto.request.OperationCreateRequest;
import com.github.seecret1.invoice_service.dto.response.OperationResponse;
import com.github.seecret1.invoice_service.entity.Operation;
import com.github.seecret1.invoice_service.exception.OperationAlreadyDeletedException;
import com.github.seecret1.invoice_service.exception.OperationNotFoundException;
import com.github.seecret1.invoice_service.mapper.OperationMapper;
import com.github.seecret1.invoice_service.repository.OperationRepository;
import com.github.seecret1.invoice_service.service.OperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationServiceImpl implements OperationService {

    private final OperationRepository operationRepository;

    private final OperationMapper operationMapper;

    @Override
    @Transactional(readOnly = true)
    public OperationResponse findById(String id) {
        log.info("Get operation by id: {}", id);
        Operation op = operationRepository.findByIdActive(id)
                .orElseThrow(() -> new OperationNotFoundException("Operation not found by id: " + id));
        log.debug("Found operation: {}", op.getId());
        return operationMapper.toResponse(op);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OperationResponse> findAll(PageModel pageModel) {
        log.info("Get all active operations page: {}, size: {}", pageModel.getNumber(), pageModel.getSize());
        Pageable pageable = pageModel.toPageRequest();
        var page = operationRepository.findAllActive(pageable);
        log.debug("Found operations totalElements: {}, totalPages: {}", page.getTotalElements(), page.getTotalPages());
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                operationMapper.toResponseList(page.getContent())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OperationResponse> findAllIncludingInactive(PageModel pageModel) {
        log.info("Get all operations including inactive page: {}, size: {}", pageModel.getNumber(), pageModel.getSize());
        Pageable pageable = pageModel.toPageRequest();
        var page = operationRepository.findAll(pageable);
        return new PageResponse<>(
                page.getTotalElements(),
                page.getTotalPages(),
                operationMapper.toResponseList(page.getContent())
        );
    }

    @Override
    @Transactional
    public OperationResponse create(OperationCreateRequest request) {
        log.info("Create operation: type={}, amountFrom={}", request.operationType(), request.amount());
        Operation entity = operationMapper.toEntity(request);
        Operation saved = operationRepository.save(entity);
        log.info("Operation created with id: {}", saved.getId());
        return operationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void softDelete(String id) {
        log.info("Soft delete operation by id: {}", id);
        Operation op = operationRepository.findByIdForUpdateActive(id)
                .orElseThrow(() -> {
                    var inactiveOpt = operationRepository.findByIdIncludingInactive(id);
                    if (inactiveOpt.isPresent() && Boolean.FALSE.equals(inactiveOpt.get().getIsActive())) {
                        return new OperationAlreadyDeletedException("Operation already soft-deleted (isActive=false) by id: " + id);
                    }
                    return new OperationNotFoundException("Operation not found by id: " + id);
                });
        op.setIsActive(false);
        operationRepository.save(op);
        log.info("Soft delete successful for operation id: {}", id);
    }

    @Override
    @Transactional
    public void hardDelete(String id) {
        log.info("Hard delete operation by id: {}", id);
        Operation op = operationRepository.findByIdIncludingInactive(id)
                .orElseThrow(() -> new OperationNotFoundException("Operation not found by id: " + id));
        operationRepository.delete(op);
        log.info("Hard delete successful for operation id: {}", id);
    }
}

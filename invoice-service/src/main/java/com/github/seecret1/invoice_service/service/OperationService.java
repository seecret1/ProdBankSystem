package com.github.seecret1.invoice_service.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.invoice_service.dto.request.OperationCreateRequest;
import com.github.seecret1.invoice_service.dto.response.OperationResponse;

public interface OperationService {

    OperationResponse findById(String id);

    PageResponse<OperationResponse> findAll(PageModel pageModel);

    PageResponse<OperationResponse> findAllIncludingInactive(PageModel pageModel);

    OperationResponse create(OperationCreateRequest request);

    void softDelete(String id);

    void hardDelete(String id);
}

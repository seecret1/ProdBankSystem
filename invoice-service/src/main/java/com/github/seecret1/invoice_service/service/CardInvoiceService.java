package com.github.seecret1.invoice_service.service;

import com.github.seecret1.common.dto.PageResponse;
import com.github.seecret1.common.model.PageModel;
import com.github.seecret1.invoice_service.dto.request.CardInvoiceCreateRequest;
import com.github.seecret1.invoice_service.dto.response.CardInvoiceResponse;
import com.github.seecret1.invoice_service.dto.response.OperationResponse;
import com.github.seecret1.invoice_service.entity.enums.CardType;

import java.util.List;

public interface CardInvoiceService {

    CardInvoiceResponse findById(String id);

    CardInvoiceResponse findByCardId(String cardId);

    List<CardInvoiceResponse> findByUserId(String userId);

    PageResponse<CardInvoiceResponse> findAll(PageModel pageModel);

    PageResponse<OperationResponse> findInvoiceOperations(String id, PageModel pageModel);

    CardInvoiceResponse create(CardInvoiceCreateRequest request);

    void softDelete(String id, String deletedBy);

    void hardDelete(String id);
}

package com.github.seecret1.invoice_service.service;

import com.github.seecret1.invoice_service.entity.CardInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InvoiceEntityService {

    Page<CardInvoice> findAllActive(Pageable pageable);

    CardInvoice findNotDeletedByIdForUpdate(String id);

    CardInvoice updateSpendingAndFreeLimits(CardInvoice invoice);

    void saveAll(List<CardInvoice> invoiceList);
}

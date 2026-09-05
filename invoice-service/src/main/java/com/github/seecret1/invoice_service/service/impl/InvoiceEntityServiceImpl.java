package com.github.seecret1.invoice_service.service.impl;

import com.github.seecret1.invoice_service.config.SpendingAndFreeLimitsConfig;
import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.exception.InvoiceAlreadyDeletedException;
import com.github.seecret1.invoice_service.exception.InvoiceNotFoundException;
import com.github.seecret1.invoice_service.repository.CardInvoiceRepository;
import com.github.seecret1.invoice_service.service.InvoiceEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceEntityServiceImpl implements InvoiceEntityService {

    private final CardInvoiceRepository cardInvoiceRepository;

    private final SpendingAndFreeLimitsConfig spendingAndFreeLimitsConfig;

    @Override
    @Transactional(readOnly = true)
    public Page<CardInvoice> findAllActive(Pageable pageable) {
        log.debug("Find all active use pageable: {}", pageable);
        var invoices = cardInvoiceRepository.findAllActive(pageable);
        log.debug("Founded page active invoices: {}", invoices);
        return invoices;
    }

    @Override
    @Transactional(readOnly = true)
    public CardInvoice findNotDeletedByIdForUpdate(String id) {
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

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CardInvoice updateSpendingAndFreeLimits(CardInvoice invoice) {
        invoice.setSpendingLimit(spendingAndFreeLimitsConfig.getMaxLimitForType(invoice.getCardType()));
        invoice.setFreeLimit(spendingAndFreeLimitsConfig.getCommissionLimitForType(invoice.getCardType()));
        return invoice;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void saveAll(List<CardInvoice> invoiceList) {
        log.info("Save all invoices, size: {}", invoiceList.size());
        cardInvoiceRepository.saveAll(invoiceList);
        log.debug("Invoice list successfully save");
    }
}

package com.github.seecret1.invoice_service.service.impl;

import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.service.InvoiceEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final InvoiceEntityService invoiceEntityService;

    @Value("${app.scheduler.pageSize}")
    private int pageSize;

    @Transactional
    public void refreshSpendingLimit() {
        int pageNumber = 0;
        int totalUpdated = 0;

        try {
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<CardInvoice> page = invoiceEntityService.findAllActive(pageable);

                if (page.isEmpty()) {
                    log.debug("No more active invoices to update");
                    break;
                }
                List<CardInvoice> invoices = page.getContent();
                log.debug("List active invoices size={}, pageNumber={}", invoices.size(), pageNumber);

                int updatedInPage = updatedSpendingLimit(invoices);
                totalUpdated += updatedInPage;

                if (page.isLast()) break;

                pageNumber++;
            }
            log.info("Scheduler completed. Total updated active invoices: {}", totalUpdated);
        } catch (Exception e) {
            log.error("Error during scheduled updated: {}", e.getMessage(), e);
        }
    }

    private int updatedSpendingLimit(List<CardInvoice> invoices) {
        int updated = 0;
        List<CardInvoice> updatedInvoices = new ArrayList<>(invoices.size());
        for (var invoice : invoices) {
            try {
                updatedInvoices.add(invoiceEntityService.updateSpendingAndFreeLimits(invoice));
                log.debug("Updated spending limit card: id={}, number={}, spendingLimit={}, freeLimit={}",
                        invoice.getId(), invoice.getInvoiceNumber(), invoice.getSpendingLimit(), invoice.getFreeLimit());
                updated++;

            } catch (Exception e) {
                log.error("Error updated limit card: id={}, error={}", invoice.getId(), e.getMessage());
            }
        }
        //TODO: добавить сохранение батчем
        return updated;
    }
}

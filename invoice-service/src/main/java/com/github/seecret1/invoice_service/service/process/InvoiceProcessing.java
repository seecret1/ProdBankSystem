package com.github.seecret1.invoice_service.service.process;

import com.github.seecret1.invoice_service.dto.order.OrderInvoiceDto;

public interface InvoiceProcessing {

    void processOrder(OrderInvoiceDto request);

    void createInvoice(OrderInvoiceDto request);
}

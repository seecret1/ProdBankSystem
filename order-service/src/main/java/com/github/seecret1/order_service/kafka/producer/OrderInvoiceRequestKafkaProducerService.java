package com.github.seecret1.order_service.kafka.producer;

import com.github.seecret1.order_service.dto.invoice.OrderInvoiceDto;

public interface OrderInvoiceRequestKafkaProducerService {

    void sendNoWait(OrderInvoiceDto message);

    void sendWithWaitToRequestInvoiceTopic(OrderInvoiceDto message);

    void sendWithWaitToInvoiceTopic(OrderInvoiceDto message);
}

package com.github.seecret1.order_service.kafka.producer;

import com.github.seecret1.order_service.dto.invoice.OrderInvoiceDto;

public interface OrderInvoiceRequestKafkaProducerService {

    void sendNoWait(OrderInvoiceDto message);

    void sendWithWait(OrderInvoiceDto message);
}

package com.github.seecret1.order_service.kafka.producer;

import com.github.seecret1.order_service.dto.card.OrderCardDto;

public interface OrderInvoiceRequestKafkaProducerService {

    void sendNoWait(OrderCardDto message);

    void sendWithWait(OrderCardDto message);
}

package com.github.seecret1.invoice_service.kafka.producer;

import com.github.seecret1.invoice_service.dto.order.BaseMessage;

public interface OrderResponseKafkaProducerService {

    void sendNoWait(BaseMessage message);

    void sendWithWait(BaseMessage message);
}

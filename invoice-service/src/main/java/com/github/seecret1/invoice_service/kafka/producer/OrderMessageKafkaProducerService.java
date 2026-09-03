package com.github.seecret1.invoice_service.kafka.producer;

import com.github.seecret1.invoice_service.dto.message.BaseMessage;

public interface OrderMessageKafkaProducerService {

    void sendNoWait(String topic, BaseMessage message);

    void sendWithWait(String topic, BaseMessage message);
}

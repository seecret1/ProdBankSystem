package com.github.seecret1.delivery_service.kafka.producer;

import com.github.seecret1.delivery_service.dto.BaseMessage;

public interface DeliveryKafkaProducerService {

    void sendNoWait(BaseMessage message);

    void sendWithWait(BaseMessage message);
}

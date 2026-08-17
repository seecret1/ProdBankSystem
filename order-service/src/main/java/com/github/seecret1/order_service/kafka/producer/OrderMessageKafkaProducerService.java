package com.github.seecret1.order_service.kafka.producer;

import com.github.seecret1.order_service.dto.BaseMessage;

public interface OrderMessageKafkaProducerService {

    void sendNoWait(BaseMessage message);

    void sendWithWait(BaseMessage message);
}

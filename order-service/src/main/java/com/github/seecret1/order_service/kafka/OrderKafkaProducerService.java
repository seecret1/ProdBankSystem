package com.github.seecret1.order_service.kafka;

import com.github.seecret1.order_service.dto.BaseMessage;

public interface OrderKafkaProducerService {

    void sendNoWait(BaseMessage message);

    void sendWithWait(BaseMessage message);
}

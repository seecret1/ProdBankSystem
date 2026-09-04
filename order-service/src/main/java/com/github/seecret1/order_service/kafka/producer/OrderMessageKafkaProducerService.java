package com.github.seecret1.order_service.kafka.producer;

import com.github.seecret1.order_service.dto.BaseMessage;

public interface OrderMessageKafkaProducerService {

    void sendNoWait(String topic, BaseMessage message);

    void sendWithWait(String topic, BaseMessage message);
}

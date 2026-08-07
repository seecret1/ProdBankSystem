package com.github.seecret1.order_service.kafka.producer;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.OrderDto;

public interface OrderKafkaProducerService {

    void sendNoWait(BaseMessage message);

    void sendWithWait(BaseMessage message);

    <T extends OrderDto> void sendToDlt(T event, Throwable error);

    <T extends OrderDto> void sendToRetry(T event, Throwable error, int attempt);
}

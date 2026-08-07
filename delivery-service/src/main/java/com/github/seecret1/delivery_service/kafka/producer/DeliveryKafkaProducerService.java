package com.github.seecret1.delivery_service.kafka.producer;

import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.order.OrderDeliveryDto;

public interface DeliveryKafkaProducerService {

    void sendNoWait(BaseMessage message);

    void sendWithWait(BaseMessage message);

    void sendToDlt(OrderDeliveryDto event, Throwable error);

    void sendToRetry(OrderDeliveryDto event, Throwable error, int attempt);
}

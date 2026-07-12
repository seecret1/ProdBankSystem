package com.github.seecret1.order_service.kafka;

import com.github.seecret1.order_service.dto.OrderMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;

public interface OrderKafkaProducerService {

    void sendNoWait(OrderCardDto event, OrderMessage message);

    void sendWithWait(OrderCardDto event, OrderMessage message);
}

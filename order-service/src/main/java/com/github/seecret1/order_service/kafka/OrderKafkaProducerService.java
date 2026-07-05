package com.github.seecret1.order_service.kafka;

import com.github.seecret1.order_service.dto.card.OrderCardResponse;
import com.github.seecret1.order_service.dto.card.OrderCreateCardDto;

public interface OrderKafkaProducerService {

    void sendNoWait(OrderCreateCardDto event, OrderCardResponse response);

    void sendWithWait(OrderCreateCardDto event, OrderCardResponse response);
}

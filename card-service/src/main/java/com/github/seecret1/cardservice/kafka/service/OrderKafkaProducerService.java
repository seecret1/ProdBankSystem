package com.github.seecret1.cardservice.kafka.service;

import com.github.seecret1.cardservice.order.message.OrderCardDto;

public interface OrderKafkaProducerService {

    void sendNoWait(OrderCardDto dto);

    void sendWithWait(OrderCardDto dto);
}

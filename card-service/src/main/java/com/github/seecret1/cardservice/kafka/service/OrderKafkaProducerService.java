package com.github.seecret1.cardservice.kafka.service;

import com.github.seecret1.cardservice.dto.order.message.OrderCardDto;

public interface OrderKafkaProducerService {

    void sendNoWait(OrderCardDto dto);

    void sendWithWait(OrderCardDto dto);
}

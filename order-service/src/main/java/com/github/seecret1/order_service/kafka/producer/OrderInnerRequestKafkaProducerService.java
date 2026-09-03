package com.github.seecret1.order_service.kafka.producer;

import com.github.seecret1.order_service.dto.card.OrderCardDto;

public interface OrderInnerRequestKafkaProducerService {

    void sendNoWait(OrderCardDto orderCardDto);

    void sendWithWait(OrderCardDto orderCardDto);
}

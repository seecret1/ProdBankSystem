package com.github.seecret1.order_service.kafka.producer;

import com.github.seecret1.order_service.dto.OrderDto;

//TODO: реализовать Декоратор от этого интерфейса
public interface OrderRequestKafkaProducerService<T extends OrderDto> {

    void sendRequestNoWait(T orderDtoRequest);

    void sendRequestWithWait(T orderDtoRequest);
}

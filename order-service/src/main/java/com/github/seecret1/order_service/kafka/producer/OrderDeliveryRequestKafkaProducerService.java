package com.github.seecret1.order_service.kafka.producer;

import com.github.seecret1.order_service.dto.delivery.OrderCardDeliveryDto;

public interface OrderDeliveryRequestKafkaProducerService extends OrderRequestKafkaProducerService<OrderCardDeliveryDto> {

    void sendRequestNoWait(OrderCardDeliveryDto deliveryDto);

    void sendRequestWithWait(OrderCardDeliveryDto deliveryDto);
}

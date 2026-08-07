package com.github.seecret1.delivery_service.service;

import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.order.OrderDeliveryDto;

public interface DeliveryService {

    BaseMessage create(OrderDeliveryDto event);

}

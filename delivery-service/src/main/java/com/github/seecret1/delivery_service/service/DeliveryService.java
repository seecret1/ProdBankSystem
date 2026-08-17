package com.github.seecret1.delivery_service.service;

import com.github.seecret1.delivery_service.dto.BaseMessage;
import com.github.seecret1.delivery_service.dto.order.OrderCardDeliveryDto;

public interface DeliveryService {

    BaseMessage create(OrderCardDeliveryDto event);

}

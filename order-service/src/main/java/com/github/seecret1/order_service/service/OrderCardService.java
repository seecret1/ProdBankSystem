package com.github.seecret1.order_service.service;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;

public interface OrderCardService {

    BaseMessage createOrder(OrderCardDto event);
}

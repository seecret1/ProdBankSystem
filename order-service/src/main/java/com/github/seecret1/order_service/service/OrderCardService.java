package com.github.seecret1.order_service.service;

import com.github.seecret1.order_service.dto.card.OrderCardResponse;
import com.github.seecret1.order_service.dto.card.OrderCreateCardDto;

public interface OrderCardService {

    OrderCardResponse createOrder(OrderCreateCardDto event);
}

package com.github.seecret1.order_service.service;

import com.github.seecret1.order_service.dto.card.OrderCardDto;

public interface OrderCardProcessingService {

    void createOrder(OrderCardDto event);
}

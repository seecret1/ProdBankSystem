package com.github.seecret1.order_service.service.processed;

import com.github.seecret1.order_service.dto.card.OrderCardDto;
import com.github.seecret1.order_service.dto.user.PersonInfo;
import com.github.seecret1.order_service.entity.OrderCard;

public interface OrderCardDeliveryService {

    OrderCard processDeliveryCourierMethod(OrderCard order, OrderCardDto event, PersonInfo personInfo, boolean office);
}

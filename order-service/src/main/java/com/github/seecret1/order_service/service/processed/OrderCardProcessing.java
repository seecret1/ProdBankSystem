package com.github.seecret1.order_service.service.processed;

import com.github.seecret1.order_service.dto.BaseMessage;
import com.github.seecret1.order_service.dto.card.OrderCardDto;

public interface OrderCardProcessing {

    void processOrder(OrderCardDto event);

    void processMessage(BaseMessage message);

    void processMessageOnInvoiceService(BaseMessage message);
}

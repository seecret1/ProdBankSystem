package com.github.seecret1.cardservice.service.processing;

import com.github.seecret1.cardservice.dto.order.message.BaseMessage;

public interface OrderProcessingService {

    void orderProcessing(BaseMessage message);
}

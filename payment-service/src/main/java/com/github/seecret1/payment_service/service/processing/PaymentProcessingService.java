package com.github.seecret1.payment_service.service.processing;

import com.github.seecret1.payment_service.dto.message.TransactionMessage;

public interface PaymentProcessingService {

    void processMessage(TransactionMessage message);
}

package com.github.seecret1.payment_service.kafka.producer;

import com.github.seecret1.payment_service.dto.message.TransactionMessage;

public interface PaymentMessageKafkaProducerService {

    void sendNoWait(TransactionMessage message);

    void sendWithWait(TransactionMessage message);
}

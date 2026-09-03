package com.github.seecret1.invoice_service.kafka.producer;

import com.github.seecret1.invoice_service.dto.message.TransactionMessage;

public interface TransactionMessageKafkaProducerService {

    void sendNoWait(TransactionMessage message);

    void sendWithWait(TransactionMessage message);
}

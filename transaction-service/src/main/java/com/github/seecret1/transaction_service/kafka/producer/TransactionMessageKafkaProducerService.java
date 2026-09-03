package com.github.seecret1.transaction_service.kafka.producer;

import com.github.seecret1.transaction_service.dto.message.TransactionMessage;

public interface TransactionMessageKafkaProducerService {

    void sendNoWait(String topic, TransactionMessage message);

    void sendWithWait(String topic, TransactionMessage message);
}

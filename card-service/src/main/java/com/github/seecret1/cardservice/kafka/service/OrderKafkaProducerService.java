package com.github.seecret1.cardservice.kafka.service;

import com.github.seecret1.cardservice.entity.Card;

public interface OrderKafkaProducerService {

    void sendNoWait(Card card, String comment, String userId);

    void sendWithWait(Card card, String comment, String userId);
}

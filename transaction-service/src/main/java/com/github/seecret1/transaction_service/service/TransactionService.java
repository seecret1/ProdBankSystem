package com.github.seecret1.transaction_service.service;

import com.github.seecret1.transaction_service.dto.message.TransactionMessage;

public interface TransactionService {

    void process(TransactionMessage message);
}

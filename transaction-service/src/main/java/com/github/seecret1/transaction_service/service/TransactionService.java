package com.github.seecret1.transaction_service.service;

import com.github.seecret1.transaction_service.dto.message.TransactionMessage;

public interface TransactionService {

    void processRequest(TransactionMessage message);

    void processResponse(TransactionMessage message);
}

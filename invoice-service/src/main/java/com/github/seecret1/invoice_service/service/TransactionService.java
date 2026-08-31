package com.github.seecret1.invoice_service.service;

import com.github.seecret1.invoice_service.dto.message.TransactionMessage;

public interface TransactionService {

    void transactionProcessing(TransactionMessage message);
}

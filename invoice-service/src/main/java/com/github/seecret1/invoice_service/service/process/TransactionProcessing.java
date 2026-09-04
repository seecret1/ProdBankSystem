package com.github.seecret1.invoice_service.service.process;

import com.github.seecret1.invoice_service.dto.message.TransactionMessage;

public interface TransactionProcessing {

    void transactionProcessing(TransactionMessage message);
}

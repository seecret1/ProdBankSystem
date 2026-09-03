package com.github.seecret1.invoice_service.service.process;

import com.github.seecret1.invoice_service.dto.message.TransactionMessage;
import com.github.seecret1.invoice_service.kafka.producer.TransactionMessageKafkaProducerService;
import com.github.seecret1.invoice_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionProcessingImpl implements TransactionProcessing {

    private final TransactionService transactionService;

    private final TransactionMessageKafkaProducerService transactionMessageKafkaProducerService;

    @Override
    public void transactionProcessing(TransactionMessage message) {
        TransactionMessage newMessage = transactionService.transactionProcessing(message);
        transactionMessageKafkaProducerService.sendWithWait(newMessage);
    }
}

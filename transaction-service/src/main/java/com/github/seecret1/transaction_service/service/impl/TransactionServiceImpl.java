package com.github.seecret1.transaction_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.dto.payment.PaymentResponse;
import com.github.seecret1.transaction_service.kafka.producer.TransactionMessageKafkaProducerService;
import com.github.seecret1.transaction_service.mapper.TransactionMapper;
import com.github.seecret1.transaction_service.repository.TransactionRepository;
import com.github.seecret1.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor //TODO: реализовать работу с батчами
public class TransactionServiceImpl implements TransactionService {

    private final TransactionMessageKafkaProducerService transactionMessageKafkaProducerService;

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final ObjectMapper objectMapper;

    @Override
    public void process(TransactionMessage message) {
        log.info("Processing messages data: {}", message.getData());

        PaymentResponse payment = objectMapper.convertValue(
                message.getData(),
                PaymentResponse.class
        );
        var entity = transactionMapper.toEntity(message, payment.id());
        transactionRepository.save(entity);
        var dto = transactionMapper.toDto(entity);
        message.setData(dto);
        transactionMessageKafkaProducerService.sendWithWait(message);
    }
}

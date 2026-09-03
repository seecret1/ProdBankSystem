package com.github.seecret1.transaction_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.transaction_service.config.kafka.properties.KafkaProperties;
import com.github.seecret1.transaction_service.dto.message.TransactionMessage;
import com.github.seecret1.transaction_service.dto.payment.PaymentResponse;
import com.github.seecret1.transaction_service.dto.transaction.TransactionDto;
import com.github.seecret1.transaction_service.entity.enums.TransactionStatus;
import com.github.seecret1.transaction_service.kafka.producer.TransactionMessageKafkaProducerService;
import com.github.seecret1.transaction_service.mapper.TransactionMapper;
import com.github.seecret1.transaction_service.repository.TransactionRepository;
import com.github.seecret1.transaction_service.service.TransactionService;
import com.github.seecret1.transaction_service.utils.DetermineTransactionStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor //TODO: реализовать работу с батчами
@Transactional(isolation = Isolation.READ_COMMITTED)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionMessageKafkaProducerService transactionMessageKafkaProducerService;

    private final KafkaProperties kafkaProperties;

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final ObjectMapper objectMapper;

    @Override
    public void processRequest(TransactionMessage message) {
        log.info("Processing messages data: {}", message.getData());

        PaymentResponse payment = objectMapper.convertValue(
                message.getData(),
                PaymentResponse.class
        );
        var entity = transactionMapper.toEntity(message, payment.id());
        transactionRepository.save(entity);
        TransactionDto dto = transactionMapper.toDto(entity);
        message.setData(dto);
        transactionMessageKafkaProducerService.sendWithWait(kafkaProperties.getInvoiceTopic(), message);

        entity.setStatus(TransactionStatus.PROCESSING);
        transactionRepository.save(entity);
    }

    @Override
    public void processResponse(TransactionMessage message) {
        log.debug("Processing response message: {}", message);
        TransactionDto dto = objectMapper.convertValue(message.getData(), TransactionDto.class);
        var transaction = transactionRepository.findById(dto.id())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found transaction by ID: " + dto.id()
                ));
        transaction.setStatus(DetermineTransactionStatus.getTransactionStatus(message.getStatus()));
        transactionRepository.save(transaction);
        transactionMessageKafkaProducerService.sendWithWait(kafkaProperties.getPaymentTopic(), message);
    }
}

package com.github.seecret1.payment_service.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.seecret1.payment_service.dto.message.TransactionMessage;
import com.github.seecret1.payment_service.dto.payment.PaymentRequest;
import com.github.seecret1.payment_service.dto.payment.PaymentResponse;
import com.github.seecret1.payment_service.dto.transaction.TransactionDto;
import com.github.seecret1.payment_service.entity.Payment;
import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.kafka.producer.PaymentMessageKafkaProducerService;
import com.github.seecret1.payment_service.mapper.PaymentManualMapper;
import com.github.seecret1.payment_service.repository.PaymentRepository;
import com.github.seecret1.payment_service.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMessageKafkaProducerService paymentMessageKafkaProducerService;

    private final PaymentRepository paymentRepository;

    private final PaymentManualMapper paymentMapper;

    private final ObjectMapper objectMapper;

    @Override
    public PaymentResponse create(String userId, PaymentRequest request) {
        log.info("Creating payment for user: {}", userId);

        Payment orderPayment = paymentMapper.toPayment(userId, request, PaymentStatus.CREATED);
        paymentRepository.save(orderPayment);
        var dto = paymentMapper.toPaymentResponse(orderPayment);
        paymentMessageKafkaProducerService.sendWithWait(paymentMapper.toTransactionMessage(orderPayment, dto));
        log.debug("Created order: {}", orderPayment);
        return dto;
    }

    @Override
    public PaymentResponse update(TransactionMessage message) {
        log.info("Update payment the user: {}", message.getUserId());

        TransactionDto transaction = objectMapper.convertValue(message.getData(), TransactionDto.class);
        var payment = paymentRepository.findById(transaction.paymentId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Not found payment by ID: " + transaction.paymentId()
                ));
        paymentRepository.save(payment);
        log.debug("Payment body: {}", payment);
        return paymentMapper.toPaymentResponse(payment);
    }
}

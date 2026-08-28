package com.github.seecret1.payment_service.service.impl;

import com.github.seecret1.payment_service.dto.payment.PaymentRequest;
import com.github.seecret1.payment_service.dto.payment.PaymentResponse;
import com.github.seecret1.payment_service.entity.Payment;
import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.kafka.producer.PaymentMessageKafkaProducerService;
import com.github.seecret1.payment_service.mapper.PaymentManualMapper;
import com.github.seecret1.payment_service.repository.PaymentRepository;
import com.github.seecret1.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMessageKafkaProducerService paymentMessageKafkaProducerService;

    private final PaymentRepository orderPaymentRepository;

    private final PaymentManualMapper paymentMapper;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PaymentResponse create(String userId, PaymentRequest request) {
        log.info("Creating payment for user: {}", userId);

        Payment orderPayment = paymentMapper.toPayment(userId, request, PaymentStatus.CREATED);
        orderPaymentRepository.save(orderPayment);
        paymentMessageKafkaProducerService.sendWithWait(paymentMapper.toTransactionMessage(orderPayment));
        log.debug("Created order: {}", orderPayment);
        return paymentMapper.toPaymentResponse(orderPayment);
    }
}

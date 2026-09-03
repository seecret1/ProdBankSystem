package com.github.seecret1.payment_service.service.processing;

import com.github.seecret1.payment_service.dto.message.TransactionMessage;
import com.github.seecret1.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessingServiceImpl implements PaymentProcessingService {

    private final PaymentService paymentService;

    @Override
    public void processMessage(TransactionMessage message) {

    }
}

package com.github.seecret1.payment_service.service;

import com.github.seecret1.payment_service.dto.payment.PaymentRequest;
import com.github.seecret1.payment_service.dto.payment.PaymentResponse;

public interface PaymentService {

    PaymentResponse create(String userId, PaymentRequest request);

}

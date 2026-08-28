package com.github.seecret1.payment_service.controller;

import com.github.seecret1.jwt_common.security.UserPrincipal;
import com.github.seecret1.payment_service.dto.payment.PaymentRequest;
import com.github.seecret1.payment_service.dto.payment.PaymentResponse;
import com.github.seecret1.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService orderPaymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PaymentRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderPaymentService.create(
                        userPrincipal.getUserId(),
                        request)
                );
    }
}

package com.github.seecret1.payment_service.mapper;

import com.github.seecret1.payment_service.dto.message.TransactionMessage;
import com.github.seecret1.payment_service.dto.payment.PaymentRequest;
import com.github.seecret1.payment_service.dto.payment.PaymentResponse;
import com.github.seecret1.payment_service.entity.Payment;
import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentManualMapper {

    public Payment toPayment(
            String userId,
            PaymentRequest dto,
            PaymentStatus status
    ) {
        return Payment.builder()
                .userId(userId)
                .sourceInvoiceId(dto.sourceInvoiceId())
                .destinationInvoiceId(dto.destinationInvoiceId())
                .amount(dto.amount())
                .currency(dto.currency())
                .paymentType(dto.type())
                .status(status)
                .build();
    }

    public TransactionMessage toTransactionMessage(
            Payment payment
    ) {
        return TransactionMessage.builder()
                .traceId(UUID.randomUUID().toString())
                .userId(payment.getUserId())
                .sourceInvoiceId(payment.getSourceInvoiceId())
                .destinationInvoiceId(payment.getDestinationInvoiceId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .timestamp(Instant.now())
                .build();
    }

    public PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getSourceInvoiceId(),
                payment.getDestinationInvoiceId(),
                payment.getAmount(),
                payment.getPaymentType(),
                payment.getCurrency()
        );
    }
}

package com.github.seecret1.payment_service.dto.message;

import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionMessage {

    private String traceId;

    private String userId;

    private String sourceInvoiceId;

    private String destinationInvoiceId;

    private BigDecimal amount;

    private String currency;

    private PaymentType paymentType;

    private PaymentStatus status;

    private Object data;

    private Instant timestamp;
}

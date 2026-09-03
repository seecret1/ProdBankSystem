package com.github.seecret1.payment_service.entity;

import com.github.seecret1.payment_service.entity.enums.PaymentStatus;
import com.github.seecret1.payment_service.entity.enums.PaymentType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "payments", schema = "payment_bank")
@AllArgsConstructor
@NoArgsConstructor
public class Payment extends AbstractBaseEntity {

    private String sourceInvoiceId;

    private String destinationInvoiceId;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
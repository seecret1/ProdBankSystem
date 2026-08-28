package com.github.seecret1.transaction_service.entity;

import com.github.seecret1.transaction_service.entity.enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.resource.transaction.spi.TransactionStatus;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@Entity
@Table(name = "transactions", schema = "transaction_bank")
@AllArgsConstructor
@NoArgsConstructor
public class Transaction extends AbstractBaseEntity {

    private String paymentId;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
}
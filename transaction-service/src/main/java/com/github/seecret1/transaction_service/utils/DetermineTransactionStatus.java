package com.github.seecret1.transaction_service.utils;

import com.github.seecret1.transaction_service.entity.enums.PaymentStatus;
import com.github.seecret1.transaction_service.entity.enums.PaymentType;
import com.github.seecret1.transaction_service.entity.enums.TransactionStatus;
import com.github.seecret1.transaction_service.entity.enums.TransactionType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DetermineTransactionStatus {

    /**
     * Определяет статус транзакции на основе статуса платежа.
     * @param paymentStatus статус платежа
     * @return соответствующий статус транзакции
     */
    public TransactionStatus getTransactionStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            return TransactionStatus.PENDING;
        }

        return switch (paymentStatus) {
            case CREATED, PROCESSING -> TransactionStatus.PENDING;
            case COMPLETED -> TransactionStatus.COMPLETED;
            case REJECTED, FAILED, CANCELLED -> TransactionStatus.FAILED;
        };
    }
}

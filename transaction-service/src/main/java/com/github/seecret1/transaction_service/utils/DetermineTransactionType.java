package com.github.seecret1.transaction_service.utils;

import com.github.seecret1.transaction_service.entity.enums.PaymentType;
import com.github.seecret1.transaction_service.entity.enums.TransactionType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DetermineTransactionType {

    /**
     * Определяет тип транзакции на основе типа оплаты.
     * @param paymentType тип оплаты
     * @return соответствующий тип транзакции
     */
    public TransactionType getTransactionType(PaymentType paymentType) {
        if (paymentType == null) {
            return null;
        }

        return switch (paymentType) {
            case CARD_PAYMENT, WITHDRAWAL, COMMISSION, INSURANCE -> TransactionType.DEBIT;

            case TRANSFER, CREDIT_PAYMENT, DEPOSIT, REFUND -> TransactionType.CREDIT;

            case CREDIT_INTEREST, DEPOSIT_INTEREST -> TransactionType.FEE;
        };
    }
}

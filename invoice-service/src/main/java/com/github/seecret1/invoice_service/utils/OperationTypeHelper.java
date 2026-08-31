package com.github.seecret1.invoice_service.utils;

import com.github.seecret1.invoice_service.entity.enums.OperationType;
import com.github.seecret1.invoice_service.entity.enums.PaymentType;
import com.github.seecret1.invoice_service.entity.enums.TransactionType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OperationTypeHelper {

    /**
     * Определяет тип операции на основе типа транзакции и типа платежа
     */
    public static OperationType determineOperationType(TransactionType transactionType, PaymentType paymentType) {

        if (transactionType == null) {
            throw new IllegalArgumentException("TransactionType cannot be null");
        }
        if (paymentType == null) {
            throw new IllegalArgumentException("PaymentType cannot be null");
        }

        return switch (transactionType) {
            case DEBIT -> mapDebitOperation(paymentType);
            case CREDIT -> mapCreditOperation(paymentType);
            case REVERSAL -> OperationType.REFUND;
            case FEE -> OperationType.COMMISSION;
        };
    }

    private static OperationType mapDebitOperation(PaymentType paymentType) {
        return switch (paymentType) {
            case CARD_PAYMENT, TRANSFER, CREDIT_PAYMENT, INSURANCE -> OperationType.PAYMENT;
            case DEPOSIT -> OperationType.DEPOSIT;
            case DEPOSIT_INTEREST -> OperationType.DEPOSIT;
            case COMMISSION -> OperationType.COMMISSION;
            case REFUND -> OperationType.REFUND;
            case WITHDRAWAL -> OperationType.WITHDRAWAL;
            case CREDIT_INTEREST -> OperationType.PAYMENT;
        };
    }

    private static OperationType mapCreditOperation(PaymentType paymentType) {
        return switch (paymentType) {
            case CARD_PAYMENT, TRANSFER, CREDIT_PAYMENT, INSURANCE -> OperationType.PAYMENT;
            case DEPOSIT -> OperationType.DEPOSIT;
            case DEPOSIT_INTEREST -> OperationType.DEPOSIT;
            case COMMISSION -> OperationType.COMMISSION;
            case REFUND -> OperationType.REFUND;
            case WITHDRAWAL -> OperationType.WITHDRAWAL;
            case CREDIT_INTEREST -> OperationType.PAYMENT;
        };
    }
}
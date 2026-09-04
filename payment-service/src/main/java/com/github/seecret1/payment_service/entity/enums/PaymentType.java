package com.github.seecret1.payment_service.entity.enums;

public enum PaymentType {

    CARD_PAYMENT,

    TRANSFER,

    CREDIT_PAYMENT,

    INSURANCE,

    REFUND,

    /// В данной версии нет реализации под эти типы оплаты
    CREDIT_INTEREST,

    DEPOSIT,

    DEPOSIT_INTEREST,

    COMMISSION,

    WITHDRAWAL
}
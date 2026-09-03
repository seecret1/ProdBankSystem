package com.github.seecret1.invoice_service.utils;

import com.github.seecret1.invoice_service.entity.CardInvoice;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class PercentUtils {

    /**
     * Возвращает процент превышения по логарифмической шкале
     * Логарифмическая формула: 10 * (1 - e^(-rawPercent/10))
     * При rawPercent = 20% -> ~9.5%
     * При rawPercent = 30% -> ~9.8%
     * Асимптотически стремится к 10%
     */
    public static BigDecimal getPercent(CardInvoice sourceInvoice, BigDecimal amount) {
        if (sourceInvoice.getSpendingLimit().compareTo(amount) >= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal excessAmount = amount.subtract(sourceInvoice.getSpendingLimit());
        BigDecimal limit = sourceInvoice.getSpendingLimit();

        BigDecimal rawPercent = excessAmount
                .divide(limit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        double rawPercentDouble = rawPercent.doubleValue();
        double percentDouble = 10 * (1 - Math.exp(-rawPercentDouble / 10));

        BigDecimal percent = BigDecimal.valueOf(percentDouble)
                .setScale(2, RoundingMode.HALF_UP);

        return percent;
    }

    public static BigDecimal getAdditionalAmount(CardInvoice sourceInvoice, BigDecimal amount) {
        BigDecimal percent = getPercent(sourceInvoice, amount);
        BigDecimal excessAmount = amount.subtract(sourceInvoice.getSpendingLimit());

        BigDecimal commissionRate = percent
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(0.5))
                .min(BigDecimal.valueOf(0.05));

        return excessAmount.multiply(commissionRate)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
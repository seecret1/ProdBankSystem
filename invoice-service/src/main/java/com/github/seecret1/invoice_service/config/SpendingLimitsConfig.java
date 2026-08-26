package com.github.seecret1.invoice_service.config;

import com.github.seecret1.invoice_service.entity.enums.CardType;
import com.github.seecret1.invoice_service.exception.InvoiceException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app.invoices")
public class SpendingLimitsConfig {

    private Map<CardType, CardLimit> limits;

    public BigDecimal getMaxLimitForType(CardType cardType) {
        CardLimit limit = limits.get(cardType);
        if (limit == null) {
            throw new InvoiceException("Spending limit is null!");
        }
        return limit.getMaxSpendingLimit();
    }

    public BigDecimal getCommissionLimitForType(CardType cardType) {
        CardLimit limit = limits.get(cardType);
        if (limit == null) {
            throw new InvoiceException("Spending limit is null!");
        }
        return limit.getCommissionSpendingLimit();
    }


    @Data
    public static class CardLimit {
        private BigDecimal maxSpendingLimit;
        private BigDecimal commissionSpendingLimit;
        private String description;
    }
}
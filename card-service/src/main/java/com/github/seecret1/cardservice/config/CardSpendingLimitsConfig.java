package com.github.seecret1.cardservice.config;

import com.github.seecret1.cardservice.entity.enums.CardType;
import com.github.seecret1.cardservice.exception.CardException;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "app.card")
public class CardSpendingLimitsConfig { //TODO: перенести в invoice-service

    private Map<CardType, CardLimit> limits;

    public BigDecimal getMaxLimitForType(CardType cardType) {
        CardLimit limit = limits.get(cardType);
        if (limit == null) {
            throw new CardException("Spending limit is null!");
        }
        return limit.getMaxSpendingLimit();
    }

    public BigDecimal getCommissionLimitForType(CardType cardType) {
        CardLimit limit = limits.get(cardType);
        if (limit == null) {
            throw new CardException("Spending limit is null!");
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
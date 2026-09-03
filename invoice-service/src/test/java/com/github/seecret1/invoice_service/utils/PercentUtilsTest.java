package com.github.seecret1.invoice_service.utils;

import com.github.seecret1.invoice_service.entity.CardInvoice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PercentUtils Unit Tests")
class PercentUtilsTest {

    private CardInvoice invoiceWithLimits(BigDecimal spendingLimit, BigDecimal freeLimit) {
        return CardInvoice.builder()
                .spendingLimit(spendingLimit)
                .freeLimit(freeLimit)
                .build();
    }

    @Test @DisplayName("getPercent: should return ZERO when amount <= spendingLimit")
    void shouldReturnZeroWhenWithinLimit() {
        CardInvoice inv = invoiceWithLimits(new BigDecimal("100000"), new BigDecimal("50000"));
        assertThat(PercentUtils.getPercent(inv, new BigDecimal("50000"))).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(PercentUtils.getPercent(inv, new BigDecimal("100000"))).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test @DisplayName("getPercent: should calculate logarithmic percent for excess")
    void shouldCalculateLogPercent() {
        CardInvoice inv = invoiceWithLimits(new BigDecimal("100000"), new BigDecimal("100000"));
        // amount 120000 -> excess 20000 -> raw 20% -> 10*(1 - e^-2) ~ 8.65
        BigDecimal percent = PercentUtils.getPercent(inv, new BigDecimal("120000"));
        assertThat(percent).isGreaterThan(BigDecimal.ZERO);
        assertThat(percent).isLessThan(new BigDecimal("10.00"));
        assertThat(percent.scale()).isEqualTo(2);
    }

    @Test @DisplayName("getPercent: should asymptotically approach 10%")
    void shouldApproachTenPercent() {
        CardInvoice inv = invoiceWithLimits(new BigDecimal("100000"), new BigDecimal("100000"));
        // Use smaller excess to demonstrate monotonic increase before saturation
        BigDecimal pSmall = PercentUtils.getPercent(inv, new BigDecimal("120000")); // 20% excess
        BigDecimal pMedium = PercentUtils.getPercent(inv, new BigDecimal("150000")); // 50% excess
        BigDecimal pLarge = PercentUtils.getPercent(inv, new BigDecimal("500000")); // 400% excess
        assertThat(pSmall).isLessThan(new BigDecimal("10.00"));
        assertThat(pMedium).isLessThan(new BigDecimal("10.00"));
        assertThat(pLarge).isLessThanOrEqualTo(new BigDecimal("10.00"));
        assertThat(pMedium).isGreaterThan(pSmall);
        assertThat(pLarge).isGreaterThanOrEqualTo(pMedium);
        assertThat(pLarge).isCloseTo(new BigDecimal("10.00"), org.assertj.core.data.Offset.offset(new BigDecimal("0.5")));
    }

    @Test @DisplayName("getAdditionalAmount: should be ZERO when within limit")
    void shouldReturnZeroAdditionalWhenWithinLimit() {
        CardInvoice inv = invoiceWithLimits(new BigDecimal("100000"), new BigDecimal("50000"));
        assertThat(PercentUtils.getAdditionalAmount(inv, new BigDecimal("50000"))).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test @DisplayName("getAdditionalAmount: should calculate commission capped at 5%")
    void shouldCapAtFivePercent() {
        CardInvoice inv = invoiceWithLimits(new BigDecimal("1000"), new BigDecimal("1000"));
        // huge excess -> percent ~10% -> commissionRate 10%/100*0.5 =0.05 capped 0.05
        BigDecimal add = PercentUtils.getAdditionalAmount(inv, new BigDecimal("100000"));
        BigDecimal excess = new BigDecimal("99000");
        assertThat(add).isEqualByComparingTo(excess.multiply(new BigDecimal("0.05")).setScale(2, java.math.RoundingMode.HALF_UP));
    }

    @Test @DisplayName("getAdditionalAmount: should scale to 2 decimals")
    void shouldScaleToTwoDecimals() {
        CardInvoice inv = invoiceWithLimits(new BigDecimal("100000"), new BigDecimal("100000"));
        BigDecimal add = PercentUtils.getAdditionalAmount(inv, new BigDecimal("120000"));
        assertThat(add.scale()).isEqualTo(2);
    }
}

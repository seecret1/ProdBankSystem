package com.github.seecret1.invoice_service.dto.order;

import com.github.seecret1.invoice_service.entity.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.kafka.common.security.oauthbearer.internals.secured.ValidateException;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class OrderInvoiceDto extends OrderDto {

    private String cardId;

    private String orderId;

    private CardType cardType;

    private String currency;

    private BigDecimal balance;

    @Override
    public void validate() {
        if (userId == null || cardId == null || traceId == null || orderId == null) {
            throw new ValidateException("Ids must not be null");
        }
        if (userId.isBlank() || cardId.isBlank() || traceId.isBlank() || orderId.isBlank()) {
            throw new ValidateException("Ids must not be blank");
        }
        if (cardType == null || orderType == null) {
            throw new ValidateException("Must not be null fields");
        }
        if (BigDecimal.ZERO.compareTo(balance) < 0) {
            throw new ValidateException("Balance must not be negative!");
        }
    }
}

package com.github.seecret1.cardservice.dto.order.message;

import com.github.seecret1.cardservice.dto.request.CardDeliveryRequest;
import com.github.seecret1.cardservice.entity.enums.CardType;
import com.github.seecret1.cardservice.dto.order.CardReceivingMethod;
import jakarta.validation.ValidationException;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCardDto extends OrderDto {

    private String cardId;

    private CardType cardType;

    private CardReceivingMethod cardReceivingMethod;

    private CardDeliveryRequest deliveryRequest;

    private String currency;

    private BigDecimal balance;

    @Override
    public void validate() {
        if (userId == null || cardId == null || traceId == null) {
            throw new ValidationException("Ids must not be null");
        }
        if (userId.isBlank() || cardId.isBlank() || traceId.isBlank()) {
            throw new ValidationException("Ids must not be blank");
        }
        if (cardType == null || orderType == null) {
            throw new ValidationException("Must not be null fields");
        }
        if ((cardReceivingMethod == CardReceivingMethod.DELIVERY_COURIER || cardType == CardType.DEBIT_PERSONAL)
                && (deliveryRequest == null || deliveryRequest.address() == null)) {
            throw new ValidationException("Must be set valid delivery request if selected the delivery method");
        }
    }
}

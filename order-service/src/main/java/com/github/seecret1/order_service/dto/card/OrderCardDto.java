package com.github.seecret1.order_service.dto.card;

import com.github.seecret1.order_service.dto.OrderDto;
import com.github.seecret1.order_service.entity.enums.CardReceivingMethod;
import com.github.seecret1.order_service.entity.enums.CardType;
import com.github.seecret1.order_service.exception.OrderValidException;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCardDto extends OrderDto {

    private String cardId;

    private CardType cardType;

    private BigDecimal spendingLimit;

    private CardReceivingMethod cardReceivingMethod;

    private CardDeliveryRequest deliveryRequest;

    @Override
    public void validate() {
        if (userId == null || cardId == null || traceId == null) {
            throw new OrderValidException("Ids must not be null");
        }
        if (userId.isBlank() || cardId.isBlank() || traceId.isBlank()) {
            throw new OrderValidException("Ids must not be blank");
        }
        if (cardType == null || orderType == null || spendingLimit == null) {
            throw new OrderValidException("Must not be null fields");
        }
        if ((cardReceivingMethod == CardReceivingMethod.DELIVERY_COURIER || cardType == CardType.DEBIT_PERSONAL)
        && (deliveryRequest == null || deliveryRequest.address() == null)) {
            throw new OrderValidException("Must be set valid delivery request if selected the delivery method");
        }
    }
}

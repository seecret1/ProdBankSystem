package com.github.seecret1.invoice_service.dto.order;

import com.github.seecret1.invoice_service.entity.enums.CardReceivingMethod;
import com.github.seecret1.invoice_service.entity.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderCardDto extends OrderDto {

    private String cardId;

    private CardType cardType;

    private String invoiceId;

    private CardReceivingMethod cardReceivingMethod;

    private CardDeliveryRequest deliveryRequest;

    @Override
    public void validate() {
        if (userId == null || cardId == null || invoiceId == null || traceId == null) {
            throw new OrderValidException("Ids must not be null");
        }
        if (userId.isBlank() || cardId.isBlank() || invoiceId.isBlank() || traceId.isBlank()) {
            throw new OrderValidException("Ids must not be blank");
        }
        if (cardType == null || orderType == null) {
            throw new OrderValidException("Must not be null fields");
        }
        if ((cardReceivingMethod == CardReceivingMethod.DELIVERY_COURIER || cardType == CardType.DEBIT_PERSONAL)
        && (deliveryRequest == null || deliveryRequest.address() == null)) {
            throw new OrderValidException("Must be set valid delivery request if selected the delivery method");
        }
    }
}

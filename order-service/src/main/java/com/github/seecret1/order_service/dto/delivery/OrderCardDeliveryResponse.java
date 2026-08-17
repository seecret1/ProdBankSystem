package com.github.seecret1.order_service.dto.delivery;

import com.github.seecret1.order_service.dto.address.AddressResponse;
import com.github.seecret1.order_service.dto.user.FullNameDto;
import com.github.seecret1.order_service.entity.enums.CardType;
import com.github.seecret1.order_service.entity.enums.PersonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCardDeliveryResponse {

    private Instant plannedDeliveryTime;

    private String orderId;

    private CardType cardType;

    private FullNameDto fullName;

    private String contactPhone;

    private AddressResponse originAddress;

    private AddressResponse destinationAddress;

    private PersonType personType;
}

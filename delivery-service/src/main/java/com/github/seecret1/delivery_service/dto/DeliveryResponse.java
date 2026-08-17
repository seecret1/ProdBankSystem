package com.github.seecret1.delivery_service.dto;

import com.github.seecret1.delivery_service.dto.address.AddressResponse;
import com.github.seecret1.delivery_service.dto.user.RecipientDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryResponse {

    private String courierId;

    private RecipientDto recipient;

    private String courierContactPhone;

    private AddressResponse originAddress;

    private AddressResponse destinationAddress;

    private Instant createdAt;
}

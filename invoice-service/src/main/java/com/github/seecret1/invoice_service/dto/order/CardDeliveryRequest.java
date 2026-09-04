package com.github.seecret1.invoice_service.dto.order;

import com.github.seecret1.invoice_service.dto.address.AddressRequest;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CardDeliveryRequest(

        @NotNull(message = "Planned delivery time must be set!")
        Instant plannedDeliveryTime,

        @NotNull(message = "Address must be set!")
        AddressRequest address

) { }

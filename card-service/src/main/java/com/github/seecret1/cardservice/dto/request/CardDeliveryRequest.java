package com.github.seecret1.cardservice.dto.request;

import com.github.seecret1.cardservice.dto.address.AddressRequest;
import jakarta.validation.constraints.*;

import java.time.Instant;

public record CardDeliveryRequest(

        @NotNull(message = "Planned delivery time must be set!")
        Instant plannedDeliveryTime,

        @NotNull(message = "Address must be set!")
        AddressRequest address

) { }

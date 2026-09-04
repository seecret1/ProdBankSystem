package com.github.seecret1.delivery_service.dto.address;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(

        @NotBlank
        String address,

        @NotBlank
        String zipCode,

        @NotBlank
        String city,

        @NotBlank
        String countryCode

) {
}

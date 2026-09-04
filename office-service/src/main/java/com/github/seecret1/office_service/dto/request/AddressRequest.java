package com.github.seecret1.office_service.dto.request;

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

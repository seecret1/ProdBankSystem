package com.github.seecret1.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressWriteDto(

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

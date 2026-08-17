package com.github.seecret1.delivery_service.dto.address;

public record AddressBaseResponse(

        String address,

        String zipCode,

        String city,

        String countryCode,

        boolean deleted
) {
}

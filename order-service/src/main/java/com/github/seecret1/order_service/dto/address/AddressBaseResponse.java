package com.github.seecret1.order_service.dto.address;

public record AddressBaseResponse(

        String address,

        String zipCode,

        String city,

        String countryCode,

        boolean deleted
) {
}

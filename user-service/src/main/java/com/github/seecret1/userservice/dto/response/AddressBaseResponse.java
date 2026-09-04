package com.github.seecret1.userservice.dto.response;

public record AddressBaseResponse(

        String address,

        String zipCode,

        String city,

        String countryCode,

        boolean deleted
) {
}

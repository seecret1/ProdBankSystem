package com.github.seecret1.order_service.dto.user;

public record PersonInfo(

        String userId,

        AddressResponse address,

        String countryCode

) {
}

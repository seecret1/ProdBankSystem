package com.github.seecret1.userservice.dto.response;

public record PersonInfo(

        String userId,

        AddressResponse address,

        String countryCode

) {
}

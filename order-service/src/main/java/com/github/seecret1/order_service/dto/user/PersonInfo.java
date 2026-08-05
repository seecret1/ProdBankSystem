package com.github.seecret1.order_service.dto.user;

import com.github.seecret1.order_service.dto.address.AddressResponse;

public record PersonInfo(

        String userId,

        AddressResponse address,

        String countryCode

) {
}

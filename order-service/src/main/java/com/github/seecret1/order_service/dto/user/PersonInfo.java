package com.github.seecret1.order_service.dto.user;

import com.github.seecret1.order_service.dto.address.AddressBaseResponse;

public record PersonInfo(

        String userId,

        FullNameDto fullName,

        String contactPhone,

        AddressBaseResponse address

) {
}
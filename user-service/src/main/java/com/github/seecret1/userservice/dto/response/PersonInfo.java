package com.github.seecret1.userservice.dto.response;

import com.github.seecret1.userservice.dto.FullNameDto;

public record PersonInfo(

        String userId,

        FullNameDto fullName,

        String contactPhone,

        AddressResponse address

) {
}

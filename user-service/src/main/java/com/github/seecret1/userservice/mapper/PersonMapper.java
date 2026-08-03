package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.response.AddressResponse;
import com.github.seecret1.userservice.dto.response.PersonInfo;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PersonMapper {

    public PersonInfo toDto(
            String userId,
            AddressResponse address,
            String countryCode
    ) {
        return new PersonInfo(userId, address, countryCode);
    }
}

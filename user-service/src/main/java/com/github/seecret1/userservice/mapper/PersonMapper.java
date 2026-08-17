package com.github.seecret1.userservice.mapper;

import com.github.seecret1.userservice.dto.FullNameDto;
import com.github.seecret1.userservice.dto.response.AddressBaseResponse;
import com.github.seecret1.userservice.dto.response.PersonInfo;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PersonMapper {

    public PersonInfo toDto(
            String userId,
            FullNameDto fullName,
            String contactPhone,
            AddressBaseResponse address
    ) {
        return new PersonInfo(userId, fullName, contactPhone, address);
    }
}

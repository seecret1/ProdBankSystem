package com.github.seecret1.userservice.dto.response;

import com.github.seecret1.userservice.dto.AddressWriteDto;

public record IndividualResponse(

        String id,

        String firstName,

        String lastName,

        String middleName,

        String email,

        String password,

        String confirmPassword,

        String passportNumber,

        String phoneNumber,

        AddressWriteDto address

) { }

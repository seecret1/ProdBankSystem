package com.github.seecret1.userservice.dto.request;

import com.github.seecret1.userservice.dto.AddressWriteDto;

public record IndividualWriteDto(

        String firstName,

        String lastName,

        String email,

        String password,

        String confirmPassword,

        String passportNumber,

        String phoneNumber,

        AddressWriteDto address

) { }
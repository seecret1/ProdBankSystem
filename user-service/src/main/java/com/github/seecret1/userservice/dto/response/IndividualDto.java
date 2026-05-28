package com.github.seecret1.userservice.dto.response;

public record IndividualDto(

    String firstName,

    String lastName,

    String email,

    String passportNumber,

    String phoneNumber,

    AddressDto address

) { }

package com.github.seecret1.userservice.dto.response;

public record IndividualResponse(

        String id,

        String firstName,

        String lastName,

        String middleName,

        String email,

        String passportNumber,

        String phoneNumber,

        AddressResponse address

) {
}

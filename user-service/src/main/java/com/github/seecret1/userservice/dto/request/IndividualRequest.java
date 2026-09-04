package com.github.seecret1.userservice.dto.request;

import jakarta.validation.constraints.*;

public record IndividualRequest(

        @NotBlank(message = "Passport number must be set!")
        @Pattern(
                regexp = "^[A-Z0-9]{10}$",
                message = "Please enter only a number that contains 10 characters, without any additional characters or spaces"
        )
        String passportNumber,

        @Size(max = 64)
        @NotBlank(message = "Phone number must be set!")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Phone number must be 10-15 digits, optionally starting with +7"
        )
        String phoneNumber,

        @NotNull(message = "Address must be set!")
        AddressRequest address

) { }
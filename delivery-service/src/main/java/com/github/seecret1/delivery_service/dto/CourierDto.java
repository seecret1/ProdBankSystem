package com.github.seecret1.delivery_service.dto;

import com.github.seecret1.delivery_service.dto.user.FullNameDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourierDto(

        @NotBlank
        String userId,

        @Valid
        @NotNull
        FullNameDto fullName,

        Boolean busy,

        @NotBlank
        String contactPhone
) { }
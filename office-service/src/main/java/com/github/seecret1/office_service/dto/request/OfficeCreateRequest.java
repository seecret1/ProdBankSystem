package com.github.seecret1.office_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OfficeCreateRequest(

        @NotBlank(message = "Office name must be set!")
        String name,

        @Size(max = 64)
        @NotBlank(message = "Office contact phone number must be set!")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Contact phone number must be 10-15 digits, optionally starting with +7"
        )
        String contactPhone,

        @NotNull(message = "Office schedule must be set!")
        List<ScheduleRequest> scheduleJson,

        @NotNull(message = "Address must be set!")
        AddressRequest address

) {
}

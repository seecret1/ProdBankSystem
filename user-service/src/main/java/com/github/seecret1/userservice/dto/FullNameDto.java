package com.github.seecret1.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullNameDto {

    @NotBlank(message = "first name must be set!")
    private String firstName;

    @NotBlank(message = "last name must be set!")
    private String lastName;

    private String middleName;
}

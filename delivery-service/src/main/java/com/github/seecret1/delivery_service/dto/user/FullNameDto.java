package com.github.seecret1.delivery_service.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FullNameDto {

    private String firstName;

    private String lastName;

    private String middleName;
}

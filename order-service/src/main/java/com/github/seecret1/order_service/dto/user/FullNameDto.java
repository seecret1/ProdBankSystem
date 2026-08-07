package com.github.seecret1.order_service.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullNameDto {

    private String firstName;

    private String lastName;

    private String middleName;
}

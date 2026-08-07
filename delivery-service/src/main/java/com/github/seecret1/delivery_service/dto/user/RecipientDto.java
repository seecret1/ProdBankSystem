package com.github.seecret1.delivery_service.dto.user;

import com.github.seecret1.delivery_service.entity.enums.PersonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecipientDto {

    private String userId;

    private FullNameDto fullName;

    private String contactPhone;

    private String officeId;

    private PersonType personType;
}

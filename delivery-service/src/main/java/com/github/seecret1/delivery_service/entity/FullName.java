package com.github.seecret1.delivery_service.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class FullName {

    private String firstName;

    private String lastName;

    private String middleName;
}

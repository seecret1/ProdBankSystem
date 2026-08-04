package com.github.seecret1.delivery_service.entity;

import com.github.seecret1.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "recipients", schema = "delivery_bank")
@AllArgsConstructor
@NoArgsConstructor
public class Recipient extends BaseEntity implements Serializable {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Embedded
    private FullName fullName;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    @Column(name = "office_id")
    private String officeId;

    @Column(name = "physical_person")
    private Boolean physicalPerson;

    @Column(name = "legal_person")
    private Boolean legalPerson;
}

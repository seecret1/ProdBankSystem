package com.github.seecret1.delivery_service.entity;

import com.github.seecret1.delivery_service.entity.enums.PersonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "recipients", schema = "delivery_bank")
@AllArgsConstructor
@NoArgsConstructor
public class Recipient extends AbstractBaseEntity implements Serializable {

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Embedded
    private FullName fullName;

    @Column(name = "contact_phone", nullable = false)
    private String contactPhone;

    @Column(name = "office_id")
    private String officeId;

    //TODO: сейчас не будет разделения на ФЛ и ЮЛ
    // Все клиенты будут ФЛ, в дальнейшем нужны буду сервисы анализа ФЛ и ЮР
    @Enumerated(EnumType.STRING)
    @Column(name = "person_type")
    private PersonType personType;
}

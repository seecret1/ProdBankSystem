package com.github.seecret1.userservice.entity;

import com.github.seecret1.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    private String username;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private String middleName;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    public void softDelete(String deletedBy) {
        setDeleted(true);
        setDeletedAt(Instant.now());
        setDeletedBy(deletedBy);
    }
}

package com.github.seecret1.delivery_service.entity;

import com.github.seecret1.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import java.time.Instant;

@Audited
@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "couriers", schema = "delivery_bank")
@AllArgsConstructor
@NoArgsConstructor
public class Courier extends BaseEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Embedded
    private FullName fullName;

    @Column(name = "busy", nullable = false)
    private Boolean busy;

    @Column(name = "contact_phone", nullable = false, unique = true)
    private String contactPhone;

    public void softDelete(String author) {
        setDeleted(true);
        setDeletedAt(Instant.now());
        setDeletedBy(author);
    }
}

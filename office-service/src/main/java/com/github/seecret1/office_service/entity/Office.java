package com.github.seecret1.office_service.entity;

import com.github.seecret1.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "offices", schema = "office_bank")
public class Office extends BaseEntity implements Serializable {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "main", nullable = false)
    private Boolean main;

    @Column(name = "contact_phone", unique = true)
    private String contactPhone;

    // TODO: заменить на мапу с сущностью графиком работы (раб. день/выходной, время работы)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedule_json", columnDefinition = "jsonb")
    private String scheduleJson;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    public void softDelete(String author) {
        setActive(false);
        setDeleted(true);
        setDeletedAt(Instant.now());
        setDeletedBy(author);
    }
}

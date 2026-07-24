package com.github.seecret1.office_service.entity;

import com.github.seecret1.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "offices", schema = "office_bank")
public class Office extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "contact_phone")
    private String contactPhone;

    // TODO: заменить на мапу с графиком работы (раб. день/выходной, время работы)
    @Column(name = "schedule_json", columnDefinition = "jsonb")
    private String scheduleJson;

    @Column(name = "active")
    private Boolean active = true;

    @OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
}

package com.github.seecret1.office_service.entity;

import com.github.seecret1.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.io.Serializable;

@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Getter
@Setter
@Entity
@Table(
        name = "addresses",
        schema = "office_bank",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_address_city_country",
                        columnNames = {"city", "address", "country_id"}
                )
        }
)
public class Address extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Size(max = 128)
    @Column(name = "address", nullable = false, length = 128)
    private String address;

    @Size(max = 32)
    @Column(name = "zip_code", nullable = false, length = 32)
    private String zipCode;

    @Size(max = 128)
    @Column(name = "city", nullable = false, length = 128)
    private String city;
}

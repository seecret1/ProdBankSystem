package com.github.seecret1.userservice.entity;

import com.github.seecret1.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Getter
@Setter
@Entity
@Table(name = "individuals", schema = "person_bank")
public class Individual extends BaseEntity {

    @Size(max = 64)
    @Column(name = "passport_number", length = 64)
    private String passportNumber;

    @Size(max = 64)
    @Column(name = "phone_number", length = 64)
    private String phoneNumber;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}

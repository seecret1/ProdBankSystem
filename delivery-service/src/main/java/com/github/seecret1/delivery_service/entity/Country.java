package com.github.seecret1.delivery_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.io.Serializable;
import java.time.Instant;

@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@DynamicInsert
@Setter
@Getter
@Entity
@Table(name = "countries", schema = "delivery_bank")
public class Country implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Size(max = 128)
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Size(max = 3)
    @Column(name = "code", nullable = false, length = 3)
    private String code;
}

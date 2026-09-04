package com.github.seecret1.order_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.io.Serializable;
import java.time.Instant;

@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Setter
@Getter
@Entity
@Table(name = "countries", schema = "order_bank")
public class Country implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ColumnDefault("true")
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ColumnDefault("(now) AT TIME ZONE 'utc'::text")
    @Column(name = "deleted_at", nullable = false)
    private Instant deletedAt;

    @Size(max = 128)
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Size(max = 3)
    @Column(name = "code", nullable = false, length = 3)
    private String code;
}

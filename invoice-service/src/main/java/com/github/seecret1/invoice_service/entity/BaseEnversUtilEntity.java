package com.github.seecret1.invoice_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Getter
@Setter
@NoArgsConstructor
@Entity
@RevisionEntity
@Table(name = "revinfo", schema = "invoice_bank_history")
public class BaseEnversUtilEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @RevisionNumber
    @Column(name = "rev")
    private long rev;

    @RevisionTimestamp
    @Column(name = "revtmstmp")
    private long revtmstmp;
}

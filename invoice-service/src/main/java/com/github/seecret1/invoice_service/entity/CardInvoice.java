package com.github.seecret1.invoice_service.entity;

import com.github.seecret1.invoice_service.entity.enums.CardType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Audited
@SuperBuilder
@Table(name = "card_invoices", schema = "invoice_bank")
@AllArgsConstructor
@NoArgsConstructor
public class CardInvoice extends BaseInvoiceEntity implements Serializable {

    @Column(name = "card_id", nullable = false, unique = true, length = 120)
    private String cardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 20)
    private CardType cardType;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "operation_id", referencedColumnName = "id")
    private Operation operation;

    @Column(name = "spending_limit", nullable = false)
    private BigDecimal spendingLimit;

    @Column(name = "free_limit", nullable = false)
    private BigDecimal freeLimit;
}

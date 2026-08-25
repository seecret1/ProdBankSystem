package com.github.seecret1.order_service.entity;

import com.github.seecret1.order_service.entity.enums.CardReceivingMethod;
import com.github.seecret1.order_service.entity.enums.CardType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "orders_card", schema = "order_bank")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCard extends BaseOrderEntity {

    @Column(name = "card_id", nullable = false, unique = true)
    String cardId;

    @Column(name = "card_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    CardType cardType;

    @Column(name = "invoice_id", unique = true)
    String invoiceId;

    @Column(name = "card_receiving_method", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    CardReceivingMethod cardReceivingMethod;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "order_card_delivery_id")
    OrderCardDelivery orderCardDelivery;

    String currency;

    BigDecimal balance;
}

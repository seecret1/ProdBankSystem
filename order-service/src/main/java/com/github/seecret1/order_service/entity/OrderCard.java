package com.github.seecret1.order_service.entity;

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

    @Column(name = "card_id", nullable = false)
    String cardId;

    @Column(name = "card_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    CardType cardType;

    @Column(name = "spending_limit", nullable = false)
    BigDecimal spendingLimit;

    //TODO: добавить работу с delivery-service
}

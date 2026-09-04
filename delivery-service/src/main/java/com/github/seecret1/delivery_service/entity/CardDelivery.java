package com.github.seecret1.delivery_service.entity;

import com.github.seecret1.delivery_service.entity.enums.CardType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import java.io.Serializable;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "card_deliveries", schema = "delivery_bank")
@AllArgsConstructor
@NoArgsConstructor
public class CardDelivery extends DeliveryBaseEntity implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;
}

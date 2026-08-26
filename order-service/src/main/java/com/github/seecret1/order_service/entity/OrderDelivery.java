package com.github.seecret1.order_service.entity;

import com.github.seecret1.order_service.entity.enums.PersonType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Entity
@Builder
@Table(name = "orders_card_delivery", schema = "order_bank")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "planned_delivery_time", nullable = false)
    Instant plannedDeliveryTime;

    @Embedded
    FullName fullName;

    @Column(name = "contact_phone", nullable = false)
    String contactPhone;

    @Column(name = "office_id")
    String officeId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "original_address_id")
    Address originalAddress;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "destination_address_id")
    Address destinationAddress;

    //TODO: сейчас не будет разделения на ФЛ и ЮЛ
    // Все клиенты будут ФЛ, в дальнейшем нужны буду сервисы анализа ФЛ и ЮР
    @Enumerated(EnumType.STRING)
    @Column(name = "person_type", nullable = false)
    PersonType personType = PersonType.PHYSICAL;
}

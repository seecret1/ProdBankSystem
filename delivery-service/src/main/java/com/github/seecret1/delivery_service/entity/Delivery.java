package com.github.seecret1.delivery_service.entity;

import com.github.seecret1.common.entity.BaseEntity;
import com.github.seecret1.delivery_service.entity.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@Entity
@SuperBuilder
@Table(name = "deliveries", schema = "delivery_bank")
@AllArgsConstructor
@NoArgsConstructor
public class Delivery extends BaseEntity implements Serializable {

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    @Column(name = "courier_id")
    private String courierId;

    @Column(name = "courier_contact_phone")
    private String courierContactPhone;

    @OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "origin_address_id", nullable = false)
    private Address originAddress;

    @OneToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "destination_address_id", nullable = false)
    private Address destinationAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    @Column(name = "delivery_duration", columnDefinition = "INTERVAL")
    private Duration deliveryDuration;

    @Column(name = "pickup_date")
    private Instant pickupDate;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    public void softDelete(String author) {
        setDeleted(true);
        setDeletedAt(Instant.now());
        setDeletedBy(author);
    }
}

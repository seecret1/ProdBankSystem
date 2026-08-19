package com.github.seecret1.delivery_service.entity;

import com.github.seecret1.delivery_service.entity.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLIntervalSecondJdbcType;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
public abstract class DeliveryBaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "planned_delivery_time", nullable = false)
    private Instant plannedDeliveryTime;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id")
    private Courier courier;

    @ManyToOne(optional = false, cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "origin_address_id", nullable = false)
    private Address originAddress;

    @ManyToOne(optional = false, cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "destination_address_id", nullable = false)
    private Address destinationAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    @Column(name = "delivery_duration", columnDefinition = "INTERVAL DAY TO SECOND")
    @JdbcType(PostgreSQLIntervalSecondJdbcType.class)
    private Duration deliveryDuration;

    @Column(name = "pickup_date")
    private Instant pickupDate;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    private Boolean deleted;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private Instant deletedAt;

    private String deletedBy;

    public void softDelete(String author) {
        setDeleted(true);
        setDeletedAt(Instant.now());
        setDeletedBy(author);
    }

    public void assignCourier(Courier courier) {
        this.courier = courier;
        this.status = DeliveryStatus.ASSIGNED;
    }
}

package com.github.seecret1.order_service.entity;

import com.github.seecret1.order_service.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "trace_id", nullable = false)
    String traceId;

    @Column(name = "user_id", nullable = false)
    String userId;

    @Column(name = "status", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    OrderStatus status;

    @Builder.Default
    Boolean deleted = false;

    Instant requestTimestamp;

    @CreationTimestamp
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;

    Instant deletedAt;

    String deletedBy;

    String comment;
}

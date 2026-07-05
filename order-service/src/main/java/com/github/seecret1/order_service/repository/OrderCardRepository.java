package com.github.seecret1.order_service.repository;

import com.github.seecret1.order_service.entity.OrderCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderCardRepository extends JpaRepository<OrderCard, UUID> {
}

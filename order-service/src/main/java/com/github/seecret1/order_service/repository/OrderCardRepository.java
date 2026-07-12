package com.github.seecret1.order_service.repository;

import com.github.seecret1.order_service.entity.OrderCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderCardRepository extends JpaRepository<OrderCard, String> {
}

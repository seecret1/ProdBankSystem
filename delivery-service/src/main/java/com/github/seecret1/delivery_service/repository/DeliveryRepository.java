package com.github.seecret1.delivery_service.repository;

import com.github.seecret1.delivery_service.entity.CardDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<CardDelivery, String> {
}

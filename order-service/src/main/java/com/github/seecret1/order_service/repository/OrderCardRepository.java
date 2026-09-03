package com.github.seecret1.order_service.repository;

import com.github.seecret1.order_service.entity.OrderCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderCardRepository extends JpaRepository<OrderCard, String> {

    @Query("""
        SELECT oc FROM OrderCard oc WHERE oc.traceId = :traceId
    """)
    OrderCard findByTraceId(String traceId);
}

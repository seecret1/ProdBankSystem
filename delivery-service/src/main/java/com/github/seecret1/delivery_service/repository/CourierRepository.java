package com.github.seecret1.delivery_service.repository;

import com.github.seecret1.delivery_service.entity.Courier;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Courier> findFirstByBusyFalseAndDeletedFalseOrderByCreatedAtAsc();

    Optional<Courier> findByUserId(String userId);

    Optional<Courier> findByIdAndDeletedFalse(String id);

    boolean existsByUserId(String userId);

    List<Courier> findAllByDeletedFalseOrderByCreatedAtAsc();
}
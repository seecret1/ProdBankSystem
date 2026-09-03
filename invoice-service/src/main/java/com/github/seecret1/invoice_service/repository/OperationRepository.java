package com.github.seecret1.invoice_service.repository;

import com.github.seecret1.invoice_service.entity.Operation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperationRepository extends JpaRepository<Operation, String> {

    @Query("""
            SELECT o FROM Operation o WHERE o.id = :id AND o.isActive = true
            """)
    Optional<Operation> findByIdActive(String id);

    @Query("""
            SELECT o FROM Operation o WHERE o.id = :id
            """)
    Optional<Operation> findByIdIncludingInactive(String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o FROM Operation o WHERE o.id = :id AND o.isActive = true
            """)
    Optional<Operation> findByIdForUpdateActive(String id);

    @Query("""
            SELECT o FROM Operation o WHERE o.isActive = true
            """)
    Page<Operation> findAllActive(Pageable pageable);

    @Query("""
            SELECT o FROM Operation o WHERE o.isActive = false
            """)
    Page<Operation> findAllInactive(Pageable pageable);
}

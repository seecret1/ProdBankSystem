package com.github.seecret1.invoice_service.repository;

import com.github.seecret1.invoice_service.entity.CardInvoice;
import com.github.seecret1.invoice_service.entity.Operation;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface CardInvoiceRepository extends JpaRepository<CardInvoice, String> {

    @Query("""
        SELECT c FROM CardInvoice c WHERE c.id = :id AND c.deleted = false
    """)
    Optional<CardInvoice> findByIdNotDeleted(String id);

    @Query("""
        SELECT c FROM CardInvoice c WHERE c.id = :id
    """)
    Optional<CardInvoice> findByIdIncludingDeleted(String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT c FROM CardInvoice c WHERE c.id = :id AND c.deleted = false
    """)
    Optional<CardInvoice> findByIdForUpdate(String id);

    @Query("""
        SELECT c FROM CardInvoice c WHERE c.deleted = false
    """)
    Page<CardInvoice> findAllNotDeleted(Pageable pageable);

    @Query("""
        SELECT c FROM CardInvoice c WHERE c.deleted = true AND c.deletedAt <= :deleteDate
    """)
    Page<CardInvoice> findDeletedBefore(Instant deleteDate, Pageable pageable);

    @Query("""
        SELECT c.operation FROM CardInvoice c WHERE c.id = :id
    """)
    Page<Operation> findAllOperationsInInvoice(String id, Pageable pageable);

    boolean existsByInvoiceNumber(String invoiceNumber);

    boolean existsByCardId(String cardId);

    Optional<CardInvoice> findByCardId(String cardId);

    Optional<CardInvoice> findByInvoiceNumber(String invoiceNumber);
}

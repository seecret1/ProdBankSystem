package com.github.seecret1.cardservice.repository;

import com.github.seecret1.cardservice.entity.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, String>, JpaSpecificationExecutor<Card> {

    @Override
    Page<Card> findAll(Pageable pageable);

    @Override
    @Query("""
    SELECT c FROM Card c WHERE c.id = :id AND c.deleted = false
    """)
    Optional<Card> findById(String id);

    @Query("""
    SELECT c FROM Card c WHERE c.deleted = false
    """)
    Page<Card> findNotDeletedCards(Pageable pageable);

    @Query("""
    SELECT c FROM Card c WHERE c.deleted = false
    """)
    Page<Card> findNotDeletedCards(Specification<Card> spec, Pageable pageable);

    @Query("""
    SELECT c FROM Card c WHERE c.deleted = false AND c.status = 'ACTIVE'
    """)
    Page<Card> findAllActive(Pageable pageable);

    @Query("""
    SELECT c FROM Card c WHERE c.dateExpiry <= :dateExpiration AND c.deleted = false
    """)
    Page<Card> findExpiryCards(LocalDate dateExpiration, Pageable pageable);

    @Query("""
    SELECT c FROM Card c 
    WHERE c.dateExpiry <= :dateExpiration AND c.deleted = false 
        AND c.status IN ('ACTIVE', 'EXTENDED')
    """)
    Page<Card> findExpiryActiveCards(LocalDate dateExpiration, Pageable pageable);

    @Query("""
    SELECT c FROM Card c WHERE c.deleted = true AND c.deletedAt <= :deleteDate
    """)
    Page<Card> findDeletedCards(Instant deleteDate, Pageable pageable);

    @Query("""
    SELECT c FROM Card c WHERE c.deleted = false AND c.userId = :userId
    """)
    Page<Card> findAllByUserId(String userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT c FROM Card c WHERE c.deleted = false AND c.numberHash = :numberHash
    """)
    Optional<Card> findByNumberHash(String numberHash);
}

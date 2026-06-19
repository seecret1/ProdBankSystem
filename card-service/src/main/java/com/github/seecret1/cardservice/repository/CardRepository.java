package com.github.seecret1.cardservice.repository;

import com.github.seecret1.cardservice.entity.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, String>, JpaSpecificationExecutor<Card> {

    @Override
    Page<Card> findAll(Specification<Card> spec, Pageable pageable);

    @Override
    Page<Card> findAll(Pageable pageable);

    Page<Card> findAllByUserId(String userId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Card> findByNumberHash(String numberHash);
}

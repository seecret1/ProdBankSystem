package com.github.seecret1.cardservice.service;

import com.github.seecret1.cardservice.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;

public interface InternalCardService {

    Page<Card> findExpiryCards(LocalDate expirationDate, Pageable pageable);

    Page<Card> findDeletedCards(Instant deletedAt, Pageable pageable);

    Page<Card> findExpiryActiveCards(LocalDate expirationDate, Pageable pageable);

    Page<Card> findAllActiveCard(Pageable pageable);
}

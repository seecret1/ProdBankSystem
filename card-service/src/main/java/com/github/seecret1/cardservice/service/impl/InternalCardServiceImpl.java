package com.github.seecret1.cardservice.service.impl;

import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.repository.CardRepository;
import com.github.seecret1.cardservice.service.InternalCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalCardServiceImpl implements InternalCardService {

    private final CardRepository cardRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Page<Card> findExpiryCards(LocalDate expirationDate, Pageable pageable) {
        log.info("Find expiry cards before period: {}", expirationDate);
        return cardRepository.findExpiryCards(expirationDate, pageable);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Page<Card> findDeletedCards(Instant deletedAt, Pageable pageable) {
        log.info("Find deleted cards before period: {}", deletedAt);
        return cardRepository.findDeletedCards(deletedAt, pageable);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Page<Card> findExpiryActiveCards(LocalDate expirationDate, Pageable pageable) {
        log.info("Find updated status cards before period: {}", expirationDate);
        return cardRepository.findExpiryActiveCards(expirationDate, pageable);
    }

    @Override
    public Page<Card> findAllActiveCard(Pageable pageable) {
        log.info("Find all active cards: {}", pageable);
        return cardRepository.findAllActive(pageable);
    }
}

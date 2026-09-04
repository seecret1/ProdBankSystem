package com.github.seecret1.cardservice.service.impl;

import com.github.seecret1.cardservice.entity.Card;
import com.github.seecret1.cardservice.entity.enums.CardStatus;
import com.github.seecret1.cardservice.service.CardSchedulerService;
import com.github.seecret1.cardservice.service.CardService;
import com.github.seecret1.cardservice.service.InternalCardService;
import com.github.seecret1.cardservice.utils.CardMaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardSchedulerServiceImpl implements CardSchedulerService {

    @Value("${app.scheduler.expiry.retention-years}")
    private long maximumYearExpiry;

    @Value("${app.scheduler.deleted.retention-years}")
    private long maximumYearDeleted;

    @Value("${app.scheduler.pageSize}")
    private int pageSize;

    private final CardService cardService;

    private final InternalCardService internalCardService;

    @Override
    @Transactional
    public void removeExpiryCards() {
        LocalDate expirationDate = LocalDate.now().minusYears(maximumYearExpiry);
        log.debug("Removing cards with expiry date before: {}", expirationDate);

        int pageNumber = 0;
        int totalDeleted = 0;

        try {
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<Card> page = internalCardService.findExpiryCards(expirationDate, pageable);

                if (page.isEmpty()) {
                    log.debug("No more expired cards to delete");
                    break;
                }
                List<Card> expiredCards = page.getContent();
                log.debug("Expired cards list size={}, pageNumber={}", expiredCards.size(), pageNumber);

                int deletedInPage = deleteCards(expiredCards);
                totalDeleted += deletedInPage;

                if (page.isLast()) break;

                pageNumber++;
            }
            log.info("Scheduler completed. Total remove expired cards: {}", totalDeleted);
        } catch (Exception e) {
            log.error("Error during scheduled cleanup: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void removeDeletedCards() {
        ZonedDateTime deletedBefore = ZonedDateTime.now(ZoneId.systemDefault())
                .minusYears(maximumYearDeleted);
        Instant deletedAtDate = deletedBefore.toInstant();
        log.debug("Removing cards with deleted date before: {}", deletedAtDate);

        int pageNumber = 0;
        int totalDeleted = 0;

        try {
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<Card> page = internalCardService.findDeletedCards(deletedAtDate, pageable);

                if (page.isEmpty()) {
                    log.debug("No more deleted cards to delete");
                    break;
                }
                List<Card> deletedCards = page.getContent();
                log.debug("Deleted cards list size={}, pageNumber={}", deletedCards.size(), pageNumber);

                int deletedInPage = deleteCards(deletedCards);
                totalDeleted += deletedInPage;

                if (page.isLast()) break;

                pageNumber++;
            }
            log.info("Scheduler completed. Total remove deleted cards: {}", totalDeleted);
        } catch (Exception e) {
            log.error("Error during scheduled cleanup: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void updateStatusExpiryCards() {
        LocalDate currentDate = LocalDate.now();
        log.debug("Update status cards with expiry current date: {}", currentDate);

        int pageNumber = 0;
        int totalUpdated = 0;

        try {
            while (true) {
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                Page<Card> page = internalCardService.findExpiryActiveCards(currentDate, pageable);

                if (page.isEmpty()) {
                    log.debug("No more expired cards to update");
                    break;
                }
                List<Card> expiredCards = page.getContent();
                log.debug("List expired cards size={}, pageNumber={}", expiredCards.size(), pageNumber);

                int updatedInPage = updatedStatus(expiredCards);
                totalUpdated += updatedInPage;

                if (page.isLast()) break;

                pageNumber++;
            }
            log.info("Scheduler completed. Total updated expired cards: {}", totalUpdated);
        } catch (Exception e) {
            log.error("Error during scheduled updated: {}", e.getMessage(), e);
        }
    }

    private int updatedStatus(List<Card> cards) {
        int updated = 0;
        for (var card : cards) {
            try {
                cardService.updateStatus(card.getNumber(), CardStatus.EXPIRED);
                log.debug("Updated card: id={}, number={}, expiry={}",
                        card.getId(), CardMaskUtils.maskCardNumber(card.getNumber()), card.getDateExpiry());
                updated++;

            } catch (Exception e) {
                log.error("Error updated card: id={}, error={}", card.getId(), e.getMessage());
            }
        }
        return updated;
    }

    private int deleteCards(List<Card> cards) {
        int deleted = 0;
        for (var card : cards) {
            try {
                cardService.hardDelete(card.getId());
                log.debug("Deleted card: id={}, number={}, expiry={}",
                        card.getId(), CardMaskUtils.maskCardNumber(card.getNumber()), card.getDateExpiry());
                deleted++;

            } catch (Exception e) {
                log.error("Error deleting card: id={}, error={}", card.getId(), e.getMessage());
            }
        }
        return deleted;
    }
}

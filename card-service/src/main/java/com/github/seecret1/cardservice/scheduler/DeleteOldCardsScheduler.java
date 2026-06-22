package com.github.seecret1.cardservice.scheduler;

import com.github.seecret1.cardservice.service.CardSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteOldCardsScheduler {

    private final CardSchedulerService cardSchedulerService;

    @Scheduled(cron = "0 0 2 * * *", zone = "Europe/Moscow")
    public void clearDbExpiredCards() {
        log.info("Starting scheduler: clearing old expired cards from DB");
        cardSchedulerService.removeExpiryCardFromDb();
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Moscow")
    public void clearDbDeletedCards() {
        log.info("Starting scheduler: clearing old deleted cards from DB");
        cardSchedulerService.removeDeletedCardFromDb();
    }
}

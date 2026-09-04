package com.github.seecret1.cardservice.scheduler;

import com.github.seecret1.cardservice.service.CardSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckStatusCardScheduler {

    private final CardSchedulerService cardSchedulerService;

    @Scheduled(cron = "${app.scheduler.status.cron}", zone = "Europe/Moscow")
    public void updateStatusExpiryCards() {
        log.info("[SCHEDULER] Starting scheduler: updating status expiry cards from DB");
        cardSchedulerService.updateStatusExpiryCards();
        log.info("[SCHEDULER] Stopping scheduler: updating status expiry cards from DB");
    }
}

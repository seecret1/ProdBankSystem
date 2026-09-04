//package com.github.seecret1.invoice_service.scheduler;
//
//import com.github.seecret1.invoice_service.service.impl.SchedulerService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class RefreshSpendingLimitScheduler {
//
//    private final SchedulerService cardSchedulerService;
//
//    @Scheduled(cron = "${app.scheduler.limit.cron}", zone = "Europe/Moscow")
//    public void clearDbExpiredCards() {
//        log.info("[SCHEDULER] Starting scheduler: refresh spending limit cards");
//        cardSchedulerService.refreshSpendingLimit();
//    }
//}

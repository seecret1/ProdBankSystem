package com.github.seecret1.userservice.schduler;

import com.github.seecret1.userservice.service.PersonSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PassportValidityScheduler {

    private final PersonSchedulerService personSchedulerService;

    @Scheduled(cron = "${app.scheduler.passport-validity.upcoming.cron}", zone = "Europe/Moscow")
    public void checkPassportValidity() {
        log.info("[Scheduler: {}]Start checkPassportValidity Scheduler", PassportValidityScheduler.class.getName());
        personSchedulerService.checkPassportValidity();
        log.info("[Scheduler: {}]End checkPassportValidity Scheduler", PassportValidityScheduler.class.getName());
    }

    @Scheduled(cron = "${app.scheduler.passport-validity.missed.cron}", zone = "Europe/Moscow")
    public void checkPassportValidityWhoMissedPassportRenewalDeadline() {
        log.info("[Scheduler: {}]Start checkPassportValidityWhoMissedPassportRenewalDeadline Scheduler",
                PassportValidityScheduler.class.getName());
        personSchedulerService.checkPassportValidityWhoMissedPassportRenewalDeadline();
        log.info("[Scheduler: {}]End checkPassportValidityWhoMissedPassportRenewalDeadline Scheduler",
                PassportValidityScheduler.class.getName());
    }
}
